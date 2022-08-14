# comic_barcode_from_webcam

A simple POC Java application using zxing to scan a comic book barcode using a webcam

## Overview

This project is a very simple implementation and example of using the [zxing library](https://github.com/zxing/zxing) to scan a comic book barcode using a webcam. This was written as a first test to see if zxing could handle the "supplementary barcode" on modern comic books - commonly called UPC or EAN extensions, EAN-2 or EAN-5 depending on length, and known in the barcode-world as addenda codes.

## Requirements

- JDK
- Maven

## QuickStart

Install requirements:

```
sudo apt install default-jdk maven
```

Download the repo:

```
git clone https://github.com/TheGrayDot/comic_barcode_from_webcam.git
```

Compile the project

```
cd comic_barcode_from_webcam
mvn package
```

Run the tool:


```
java -jar target/comic_barcode_from_webcam-1.0-shaded.jar
```
