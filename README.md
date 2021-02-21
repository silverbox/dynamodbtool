# DynamoDB Tool

## Information

Sorry, It's before the 1st release. but certainly you can use it under your responsibility.

## What is this tool?

It's utility tool to view AWS DynamoDB data.
Not only AWS DynamoDB but also local DynamoDB available. It's not service with browser but stand alone tool.

## Fearures

- Not only AWS DynamoDB but also local DynamoDB available.
- You can use it without any web browser.
- Show basic info of the table(record count, size, key information)
- Copy selected cell string to PC'S clipboard.
- Detail information dialog available.
- Update the record. (Especially, please use it under your responsibility)

## Requirement

- java8 / java11 runtime to execute
- maven to build  
(if someone share the built file, it's not required)

## How to start to use the tool.

outline

1. build the project
2. execute the built jar file

### build the project

Following both way is available.

 - ex1. Use maven

```
git clone https://github.com/silverbox/dynamodbtool.git
cd dynamodbtool
mvn package
```

 - ex2. Use Eclipse  
prepare for eclpse

 ```
git clone https://github.com/silverbox/dynamodbtool.git
cd dynamodbtool
mvn eclipse:eclipse
# after that
# launch Eclipse and Import the project.
# Export as Runable jar file.
 ```

### execute the jar file

if your jar file name is not ```dynamodbtool.jar```, please replace the name in the following command to your jar file name.

#### java8

1. pleasse run the following command.

```
java -jar dynamodbtool.jar
```

#### java11

1. at first, please download the required library from the following site(target is not jmods but SDK). and then unzip the file.  
https://gluonhq.com/products/javafx/

2. confirm the location of ```lib``` folder under the unziped folder.

3. please run build the jar file with some argument like as below. the word ```{PATH_TO_LIB}``` means your folder you confirmed at previous step.

```
java --module-path {PATH_TO_LIB} --add-modules=javafx.controls,javafx.fxml -jar dynamodbtool.jar
```

## How to use

1. launch the tool
1. select which connection you use. (AWS DynamoDB / local DynamoDB)
1. input load condition of table list.
1. enter Return. then you can see the table list.
1. double click the table name you want to view.
1. you can see the information of target table at right up area.
1. specify the value of condition if required.
1. click the ```Load``` button.
1. You can range select. and copy selected cells string to clipboard.
1. If you double click a Table record, then you can see detail dialog.
1. You can edit the value by edit the field(Scalar type value).
1. You can edit the value by open child dialog(Set, Document type value).