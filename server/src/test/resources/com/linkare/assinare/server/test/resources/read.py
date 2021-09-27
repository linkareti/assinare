#!/usr/bin/python

import os
import sys
import json
import argparse
import traceback
import time

REPO_FOLDER = ''

parser = argparse.ArgumentParser(description='Read a file from the ether.')
parser.add_argument('docName')
parser.add_argument('--optiona')
parser.add_argument('--optionb')
parser.add_argument('--timeout', type=int)

doc_params = parser.parse_args()
doc_name = doc_params.docName
doc_path = os.path.join(REPO_FOLDER, doc_name)

if doc_params.timeout:
    time.sleep(doc_params.timeout)

try:
    with open(doc_path, 'rb') as f:
        read_data = f.read()
except FileNotFoundError:
    traceback.print_exc()
    sys.exit(101)

with open(doc_path + '.params', 'w') as f:
     f.write(str(doc_params))

sys.stdout.buffer.write(read_data)
