# Rewrite_Rule_Generation
Generates graph-transformation rules for the graph transformation toolset [Groove](https://groove.ewi.utwente.nl/about) and rewrite rules for the [Maude system](https://maude.cs.illinois.edu/w/index.php/The_Maude_System) from different behavioral languages (most importantly BPMN).

The generation of graph transformation rules for BPMN models is described in the [wiki](https://github.com/timkraeuter/Rewrite_Rule_Generation/wiki).

## Contained projects
This repository contains multiple related projects.
### Generator
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Generator&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Generator)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Generator&metric=coverage)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Generator)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Generator&metric=bugs)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Generator)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Generator&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Generator)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Generator&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Generator)

The project **Generator** contains the source code to generate Groove/Maude rules from different behavioral languages, for example, BPMN.
### Generation-ui
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Generation-UI&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Generation-UI)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Generation-UI&metric=bugs)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Generation-UI)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Generation-UI&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Generation-UI)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Generation-UI&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Generation-UI)

The project **Generation-ui** contains the ui code for the generation of Groove rules from BPMN files. Including model-checking of BPMN models. Maude generation is not yet accessible through the UI.
### Server
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Server&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Server)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Server&metric=coverage)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Server)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Server&metric=bugs)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Server)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Server&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Server)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=timkraeuter_Groove_Rule_Generation_Server&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=timkraeuter_Groove_Rule_Generation_Server)

The project **server** contains the webservices used by the generation-ui for BPMN rule generation and model-checking.

## BPMN generation
A demo version of the tool is hosted [here](https://bpmnanalyzer.whitefield-c9fed487.northeurope.azurecontainerapps.io/).

[![Tool screenshot](./documentation/impl.png)](https://bpmnanalyzer.whitefield-c9fed487.northeurope.azurecontainerapps.io/)

Go [here](/server/README.md) if you want to run the tool to generate graph-transformation rules for BPMN locally

## Code style & Static analysis
I use the [Google Java Code Style](https://google.github.io/styleguide/javaguide.html) in my project.
The style is automatically enforced using [google-java-format](https://github.com/google/google-java-format) through the [spotless](https://github.com/diffplug/spotless/tree/main/plugin-gradle#google-java-format) gradle plugin and can also be installed in your IDE.
For the UI part written in typescript, HTML, and CSS, I use the [Prettier](https://prettier.io/) code formatter.

Sonarcloud is used for static analysis click on lines of code, coverage, bugs, code smells, and vulnerabilities above.
In addition, I experiment with Google's [Error Prone](https://errorprone.info/), but it mostly finds the same issues as sonarcloud.
