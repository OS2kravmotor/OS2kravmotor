#! /usr/bin/env python
# findUnusedProperties.py - Scans for unused texts in messages.properties
# usage: copy to DataSquid root folder and run without arguments
# example: $ python findUnusedProperties.py

import os
import fnmatch
import re

path = os.getcwd()
# get list of paths to .html files
htmlFiles = [os.path.join(dirpath, f)
    for dirpath, dirnames, files in os.walk(path)
    for f in fnmatch.filter(files, '*.html')]

# get list of paths to files named message.properties
propertyFiles = [os.path.join(dirpath, f)
    for dirpath, dirnames, files in os.walk(path)
    for f in fnmatch.filter(files, 'messages_en_US.properties')]

# locate all #{???} entries, and return them as a set
def step1():
    entries = set ()
    for path in htmlFiles:
        htmlFile = open(path)
        for htmlFileLine in htmlFile:
            result  = re.search('\#\{(html.*?)\}.' , htmlFileLine)
            if hasattr(result,'group'):
                entries.add(result.group(1))
    return entries

# locate all html.* properties found in messages.properties files, return them as a set
def step2():
    entries = set()
    for propertyFilePath in propertyFiles:
        fo = open(propertyFilePath)
        for line in fo:
            result = re.search('(html.*?)=',line)
	    if hasattr(result,'group'):
	        entries.add(result.group(1))
    return entries

# output only properties from step 2 that are not in step 1
print('unused tags: ');
print(list(step2()-step1()))
