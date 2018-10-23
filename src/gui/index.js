const fs = require('fs');
const child = require('child_process');
const readline = require('readline');
const express = require('express');
const busboy = require('connect-busboy');
const pug = require('pug');

const app = express();

function prom(func) {
	return new Promise((res, rej) =>
		func((err, ...data) =>
			err ? rej(err) : res(...data)
		)
	);
}

var filekeys = {svr: true, mcp: true, btw: true};
var uploads = {};

app.use(busboy());
app.post('/upload', (req, res) => {
	var ws;
	req.busboy.on('file', (name, file) => {
		if(!filekeys[name]) throw("disallowed field");
		var dest = __dirname + '/../' + name;
		dest = {new: dest + ".new", zip: dest + ".zip"};
		ws = fs.createWriteStream(dest.new);
		file.pipe(ws);
		ws.on('close', () => {
			var size;
			prom(cb => fs.stat(dest.new, cb))
				.then(s => {
					size = s.size;
					if(size)
						return prom(cb => fs.rename(dest.new, dest.zip, cb))
							.then(() => uploads[name] = {size});
				})
				.catch(err => uploads[name] = {err});
		});
	});
	req.busboy.on('finish', () => res.redirect('/'));
	req.pipe(req.busboy);
});

var idx_wrap = pug.compile(`
head
  title Better With Renewables Build GUI
  style(type="text/css").
    body{font-family:sans-serif}
    fieldset{background-color:#f8f8f8;border-color:#ccc}
    .nb{border:hidden;background:transparent}
    .l{float:left;display:inline-block}
    .r{float:right;display:inline-block}
    input{display:inline-block;min-width:30vw;max-width:100%;
       padding: 0.25in 0.25in 0.25in 0.25in;border:1px solid #ccc;background:#eee;
       text-align:left}
body
  h2.r Better With Renewables
  | !{body}
`);
function pugwrap(t) {
	t = pug.compile(t);
	return function(...data) {
		return idx_wrap({body: t(...data)});
	};
}

var idx_upload = pugwrap(`
mixin link(uri)
  div: a(target="_blank" href=uri) #{uri}
mixin stat(st)
  if st && st.err
    .l(style="color:#800") ERROR: #{st.err}
  else if st && st.size
    .l(style="color:#080") Uploaded #{st.size} bytes
  else
    .l(style="color:#862") Awaiting upload...
mixin upblk(id, title, link)
  fieldset
    legend #{title}
    +link(link)
    +stat(uploads[id])
    .r: input(type="file", name=id)
h1 Upload
form(method="POST" enctype= "multipart/form-data" action="upload")
  +upblk("svr", "Minecraft Server Jar", "https://mcversions.net/")
  +upblk("mcp", "Mod Coder's Pack Zip", "https://minecraft.gamepedia.com/Programs_and_editors/Mod_Coder_Pack")
  +upblk("btw", "Better Than Wolves Zip", "http://sargunster.com/btwforum/viewforum.php?f=3&sid=683a86bf68d87dfff20f88158ecd756a")
  fieldset.nb: input.r(type="submit" value="Upload file(s)...")
if(uploads.ready)
  form(method="POST" action="build")
    fieldset.nb: input.r(type="submit" value="START BUILD")
else
  fieldset.nb: input.r(disabled="disabled" style="color:#888" type="submit" value="START BUILD")
`);

var phase = idx_upload;
app.use('/', (req, res, next) => {
	uploads.ready = !'svr mcp btw'.split(' ').find(i => !uploads[i] || !uploads[i].size);
	res.setHeader("Content-Type", "text/html");
	res.send(phase({uploads}));
});

let port = 4280;
app.listen(port, () => console.log(`
========================================
*** Better with Renewables Installer ***
----------------------------------------

Web server is now listening on:
	http://localhost:${port}/

----------------------------------------
`));
