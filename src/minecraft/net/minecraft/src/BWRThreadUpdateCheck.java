package net.minecraft.src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// This class represents a background thread that checks the BWR website
// (currently hosted on gitlab) for a new version of BWR on server startup,
// and once again periodically as the server is running.  Any update version
// information found is reported to the mod_BetterWithRenewables class, to be
// announced to users as appropriate.
public class BWRThreadUpdateCheck extends Thread {
	// Shortcut for logging a message relevant to the update check subsystem.
	// a prefix is added automatically, as these messages will tend to
	// interleave with other messages in other threads.
	private void log(String msg) {
		BWREngineCore.getInstance().log("Update Check: " + msg);
	}

	// This method performs a single update check, contacting the BWR website
	// and reading the latest released source code. Exceptions are ignored.
	private void updateCheck() {
		try {
			log("Starting...");
			Pattern VerRx = Pattern.compile("^\\s*BWR\\s*=\\s*(0\\..*)");
			String UpdVer = null;

			// Contact the BWR website and start downloading the latest source code
			// for the BWREngineCore class, which contains the static hard-coded
			// version numbers.
			URL Url = new URL("https://gitlab.com/btwbwr/bwr/raw/master/Makefile");
			URLConnection URLConn = Url.openConnection();
			URLConn.setUseCaches(false);
			BufferedReader BR = new BufferedReader(new InputStreamReader(URLConn.getInputStream()));

			// Scan through the source code line-by-line and identify the places in the
			// source code where the version information is specified.
			String Line;
			while ((Line = BR.readLine()) != null) {
				// Look for the version number.
				Matcher VerMatch = VerRx.matcher(Line);
				while (VerMatch.find()) {
					UpdVer = VerMatch.group(1);
					log("Found version " + UpdVer);
					break;
				}
			}
			BR.close();

			// If there is a new version available, report it to the server
			// operator on the server console, and to the mod core.
			if ((UpdVer != null) && !BWRVersionInfo.BWR_VERSION.equals(UpdVer)) {
				UpdVer = BWREngineCore.BWR_ABBREV.toUpperCase() + " VERSION " + UpdVer + " IS NOW AVAILABLE";
				BWREngineCore.versionUpdateAlert = UpdVer;
				log(UpdVer);
			} else
				BWREngineCore.versionUpdateAlert = null;

			log("Complete");
		} catch (Exception ex) {
			// Log any exceptions caught (e.g. network errors), but
			// ignore them and let the thread keep running; we'll try
			// again next cycle, and maybe it will work then.
			log(ex.toString());
		}
	}

	// This is the main update check loop, run in a separate thread by Launch.
	public void run() {
		try {
			// Run forever.
			while (true) {
				// Do an update check immediately when the server is
				// first started, then once again each cycle.
				updateCheck();

				// Wait 4 hours between cycles, to keep traffic at the
				// BWR website under control. If the server is stopped
				// at any time, try to gracefully stop the thread.
				for (int S = 0; S < 14400; S++) {
					Thread.sleep(1000);
				}
			}
		} catch (Exception ex) {
			// The update checker is not a vital part of the BWR server.
			// If anything goes wrong, just log the issue and let the
			// thread die. Note that this should NOT include network issues,
			// as there's a separate trap for that in updateCheck().
			log(ex.toString());
		}
	}

	// Start the update checker in a background thread. Called
	// by BWREngineCore.initialize(), only once on startup.
	public static void launch() {
		(new BWRThreadUpdateCheck()).start();
	}
}
