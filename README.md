NCSO Demo Website
-----------------

The NCSO Website consists of 2 components, the front end using Angular.js and the back end API using the Play! framework
and Scala.


Building the Angular Front End
==============================

From the NCSO Website root:

`$ cd app/frontend/angularjs`

`$ npm install`

`$ bower install`

Note: If you have a bower git error run the following command:

`$ git config --global url."https://".insteadOf git://`



Deploying the Front End Locally
===============================

To deploy the front end locally and keep running in live reload mode run:

`$ grunt serve`


Packaging the Front End Into Static Files For Production Use with the Back End API
==================================================================================

Running the grunt build task will deploy minified, production ready versions of the angular app in the public/javascripts/dist/ directory.

`$ grunt build`


Running the Back End API
========================

`$ sbt run`

Navigate to http://localhost:9000/ and you can access the most recent front end deployed files.






