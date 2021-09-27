#!/usr/bin/python

import os
import sys
import argparse
import pathlib

REPO_FOLDER = ''

parser = argparse.ArgumentParser(description='Write a file to the ether.')
parser.add_argument('docName')
parser.add_argument('--optiona')
parser.add_argument('--optionb')

doc_params = parser.parse_args()
doc_name = doc_params.docName
signed_doc_name = pathlib.PurePath(doc_name).stem + '.signed' + pathlib.PurePath(doc_name).suffix
signed_doc_path = os.path.join(REPO_FOLDER, signed_doc_name)

read_data = sys.stdin.buffer.read()

with open(signed_doc_path, 'wb') as f:
    f.write(read_data)
