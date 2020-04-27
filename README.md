# Information retrieval models for contact recommendation

This repository contains the code needed to reproduce the experiments of the paper:

> J. Sanz-Cruzado, P. Castells, C. Macdonald, I. Ounis. [Effective Contact Recommendation in Social Networks by Adaptation of Information Retrieval Models](http://ir.ii.uam.es/pubs/ipm2020.pdf). Information Processing & Management, 2020. In Press.

## Authors
Information Retrieval Group at Universidad Autónoma de Madrid
- Javier Sanz-Cruzado (javier.sanz-cruzado@uam.es)
- Pablo Castells (pablo.castells@uam.es)

Terrier Group at University of Glasgow
- Craig Macdonald (craig.macdonald@glasgow.ac.uk)
- Iadh Ounis (iadh.ounis@glasgow.ac.uk)

## Software description
This repository contains all the needed classes to reproduce the experiments explained in the paper.

## Installation
In order to install this program, you need to have Maven (https://maven.apache.org) installed on your system. Then, download the files into a directory, and execute the following command:
```
mvn compile assembly::single
```
If you do not want to use Maven, it is still possible to compile the code using any Java compiler. In that case, you will need the following libraries:
- Ranksys version 0.4.3: http://ranksys.org
- Colt version 1.2.0: https://dst.lbl.gov/ACSSoftware/colt
- Google MTJ version 1.0.4: https://github.com/fommil/matrix-toolkits-java
- Terrier version 5.1: http://terrier.org/
- FastUtil version 8.3.0: http://fastutil.di.unimi.it/
