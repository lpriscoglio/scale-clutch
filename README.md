# scale-clutch
System for dynamically sizing an Apache Spark Cluster

This Java program allows to automatically resize an Apache Spark Cluster hosted on an Amazon AWS infrastructure, by requesting a greater number of virtual machines or decommissioning the existing ones, depending on the overall load of the Spark system. Load is defined by historical usage (both hourly and global), number of CPU cores currently requested and application type.

In order for it to be used, it is important to properly set a few parameters, indicated in the code with comments.

Disclaimer: this program is a Proof of Concept of the feasibility of such a feature, it is obviously not meant for a production enviroment.
For comments, mail to lpriscoglio@gmail.com
