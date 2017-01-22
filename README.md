Tracker
=======

Tracker is to help developers to jumpstart developping anykind of mobility tracking applications. 

Tracker lets any type or number of GPS devices to post their locations to a nodejs server through a ReST service.

This consists of several main modules

  1. HTML5+AngularJS Front-end
  2. NodeJS+MongoDB API for collecting and quering tracker data
  3. Php+MySQL back-end to manage the trackers & etc. 
  4. Android app to push data to the API 
  5. Documentation

 
 
##Installation Guide 

1. Clone the repo using
  ```
git clone https://github.com/scorelab/Tracker.git
```

2. Install node and mongodb:
  * On macOS:
    1. install [Homebrew](http://brew.sh), a package manager, with `/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"`
    2. add Homebrew's install location to your `$PATH`:  

      ```bash
      export PATH="/usr/local/bin:$PATH"
      ```
    3. install node with `brew install node`
    4. install mongodb with `brew install mongodb`
  * On Linux distros:
    - use an appropriate package manager to install mongodb and node.
    - Ubuntu/Debian: `apt-get install node` and `apt-get install mongodb`
    - Fedora: `sudo dnf install node` and `sudo dnf install mongodb`

3. cd to the mobility-track-nodejs folder. Do an npm install. It will install all the pakages.
  ```
cd mobility-track-nodejs
```

  ```
  npm install
  ```

4. Run mongodb with `mongod` (**mongo d**aemon). Default path is set to `/data/db` ( On Windows this would be `C:\data\db`). You can change it when you are running.

5. Install grunt-cli with
  ```
  npm install -g grunt-cli
  ```

6. Now run grunt, it will start the node server and listen to port 3000.
```
grunt
```

## OSX Installation Guide 

OSX is also quite a simple installation. Skip the sections if you already 
have that software installed.

### Install Homebrew

1. Run the installer
```
/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
```

2. Then run `brew update` to make sure Homebrew is up to date
```
brew update
```

3. Add Homebrew to the dollar $PATH so it can be accessed properly
```
echo "export Path=/usr/local/bin:$PATH" >> ~/.bash_profile && source  ~/.bash_profile
```

4. (Optional but suggested) Run `brew doctor` to make sure that homebrew is 
ready to work.
```
brew doctor
```

### Node.js

Use homebrew to install node.js and npm
```
brew install node
```

### Grunt

1. Install Grunt's CLI with npm (If this did not work something went wrong 
with the Node installation)
```
npm install -g grunt-cli
```

### MongoDB

1. Use homebrew to install MongoDB
```
brew install mongodb
```

2. Setup the default 'data/db' folder for mongoDB (use `sudo` if you have to)
```
mkdir -p /data/db
```

3. Ensure the current user running mongoDB has read and write permissions for
 the data/db
 
### Running the app
 
1. Ensure you are in /mobility-track-nodejs/ for the following steps
 
2. Run mongoDB (use `sudo` if you need to)
```
mongod
```

3. Run grunt to finish the setup and start the server
```
grunt
```

4. Navigate to `localhost:3000` to access the site on your web browser.

Now get started developing!


