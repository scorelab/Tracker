module.exports = function(grunt) {
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    jshint: {
      options: {
        jshintrc: true
      },
      all: {
        src: ['*.js', 'public/js/*.js']
      }
    },
    watch: {
      js: {
        files: ['*.js', 'public/js/*.js'],
        tasks: ['jshint']
      }
    },
    nodemon: {
      dev: {
        script: 'server.js',
        options: {
          args: [],
          ignoredFiles: ['node_modules/**'],
          ignore: ['public/**'],
          watchedExtensions: ['js'],
          debug: true,
          delayTime: 1,
          env: {
            PORT: 3000
          },
          cwd: __dirname
        }
      }
    },
    concurrent: {
      tasks: ['nodemon', 'watch'],
      options: {
        logConcurrentOutput: true
      }
    }
  });

  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-nodemon');
  grunt.loadNpmTasks('grunt-concurrent');
  
  //Development task
 grunt.task.registerMultiTask('grunt', ['build','testServerJS'], function() {
      process.exit(0);});
 }
  grunt.registerTask('default', ['jshint', 'concurrent']);
