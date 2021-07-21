const fs = require('fs');
const path = require('path');
const packageJson = require('../package.json');
const extraDependencies = require('../src/config/config-dependencies.json');

Object.entries(extraDependencies).forEach(([ packageName, version ]) => {
  packageJson.dependencies[packageName] = version;
});

fs.writeFile(path.resolve(__dirname, '../package.json'), JSON.stringify(packageJson, null, 2), function(err) {
  if(err) {
    console.log(err);
  } else {
    console.log('Dependencies merged in package.json');
  }
});
