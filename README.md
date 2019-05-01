![Build status](https://travis-ci.org/bodastage/boda-huaweimmlparser.svg?branch=master)

# boda-huaweicmobjectparser
Coverts Huawei 2G,3G, and 4G CFGMML dumps to csv.

# Usage
```
usage: java -jar boda-huaweimmlparser.jar
Parses Huawei CFGMML files to csv

 -c,--parameter-config <PARAMETER_CONFIG>   parameter configuration file
 -h,--help                                  show help
 -i,--input-file <INPUT_FILE>               input file or directory name
 -o,--output-directory <OUTPUT_DIRECTORY>   output directory name
 -p,--extract-parameters                    extract only the managed
                                            objects and parameters
 -v,--version                               display version

Examples:
java -jar boda-huaweimmlparser.jar -i cfgmml_dump.txt -o out_folder
java -jar boda-huaweimmlparser.jar -i input_folder -o out_folder

Copyright (c) 2019 Bodastage Solutions(http://www.bodastage.com)
```

# Download and installation
The lastest compiled jar file is availabled in the dist directory. Alternatively, download it directly from [here](https://github.com/bodastage/boda-huaweimmlparser/raw/master/dist/boda-huaweimmlparser.jar).

# Requirements
To run the jar file, you need Java version 1.6 and above.

# Getting help
To report issues with the application or request new features use the issue [tracker](https://github.com/bodastage/boda-huaweimmlparser/issues). For help and customizations send an email to info@bodastage.com.

# Credits
[Bodastage](http://www.bodastage.com) - info@bodastage.com

# Contact
For any other concerns apart from issues and feature requests, send an email to info@bodastage.com.

# Licence
This project is licensed under the Apache 2.0 licence.  See LICENCE file for details.
