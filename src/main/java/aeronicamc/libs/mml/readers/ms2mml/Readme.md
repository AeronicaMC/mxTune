**How to generate the java classes needed to read ms2mml XML files.**

Notes:
 * [XSD/XML Schema Generator](https://www.freeformatter.com/xsd-generator.html#ad-output)
 Paste in an ms2mml XML file, then use the 'Russian Doll' XSD Design option. Save the xsd to ms2mml.xsd and run the JAXB compiler to generate the classes.
  * JAXB Compiler options used: xjc -d src -p aeronicamc.libs.mml.readers.ms2mml ms2mml.xsd
  * Make sure to move the generate classes to the proper package as needed.