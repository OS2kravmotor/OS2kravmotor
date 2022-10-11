#!/bin/bash
cd ..
jython messagebundle-scripts/findUnusedProperties.py
jython messagebundle-scripts/messagePropertiesCheck.py
