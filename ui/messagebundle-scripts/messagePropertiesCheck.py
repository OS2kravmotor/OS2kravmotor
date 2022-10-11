# syntax: jython messagePropertiesCheck.py
# checks for entries in one message bundle that does not exist in the other

import sys
import java
from java.util import Properties
from java.io import FileInputStream
from java.lang import System

relative_path = 'src/main/resources/'

# message.properties filenames
propertiesFileEN = relative_path + 'messages_en_US.properties'
propertiesFileDK = relative_path + 'messages_da_DK.properties'

# Properties API (java.util.Properties)
propertiesEN = Properties();
propertiesDK = Properties();

propertiesEN.load(FileInputStream(propertiesFileEN))
propertiesDK.load(FileInputStream(propertiesFileDK))

keysEN = propertiesEN.stringPropertyNames()
keysDK = propertiesDK.stringPropertyNames()

diferences = list(set(keysEN) - set(keysDK) )
for d in diferences:
	print('found in EN but does not exist in DK: ' + d)

diferences = list(set(keysDK) - set(keysEN))
for d in diferences:
        print('found in DK but does not exist in EN: ' + d)

