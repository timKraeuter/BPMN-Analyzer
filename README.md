# Rewrite_Rule_Generation
Generates graph-transformation rules for the graph-transformation tool [Groove](https://groove.ewi.utwente.nl/about) and rewrite rules for the [maude system](https://maude.cs.illinois.edu/w/index.php/The_Maude_System) from different behavioral languages (most importantly BPMN).

## Contained projects
This repository contains multiple related projects.
### Generator
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Generator&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Generator)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Generator&metric=coverage)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Generator)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Generator&metric=bugs)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Generator)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Generator&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Generator)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Generator&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Generator)

The project **Generator** contains the source code to generate Groove rules from different behavioral languages, for example, BPMN.
### Generation-ui
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Generation-UI&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Generation-UI)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Generation-UI&metric=bugs)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Generation-UI)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Generation-UI&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Generation-UI)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Generation-UI&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Generation-UI)

The project **Generation-ui** contains the ui code for the generation of Groove rules from BPMN files. Including model-checking of BPMN models.
### Server
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Server&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Server)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Server&metric=coverage)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Server)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Server&metric=bugs)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Server)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Server&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Server)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Server&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Server)

The project **server** contains the webservices used by the generation-ui for BPMN rule generation and model-checking.

## BPMN generation
A demo version of the tool is also hosted [here](https://bpmn-analyzer.herokuapp.com/).

[![Tool screenshot](./documentation/impl.png)](https://bpmn-analyzer.herokuapp.com/)

Go [here](/server/README.md) if you want to run the tool to generate graph-transformation rules for BPMN locally
