#!/usr/bin/python

import os
import sys
import argparse
from pathlib import Path
from pathlib import PurePath

REPO_FOLDER = Path.home().joinpath('.assinare/repo')

parser = argparse.ArgumentParser(description='Write a file to the ether.')
parser.add_argument('docName')
parser.add_argument('--optiona')
parser.add_argument('--optionb')

doc_params = parser.parse_args()
doc_name = doc_params.docName
signed_doc_name = PurePath(doc_name).stem + '.signed' + PurePath(doc_name).suffix
signed_doc_path = os.path.join(REPO_FOLDER, signed_doc_name)

read_data = sys.stdin.buffer.read()

with open(signed_doc_path, 'wb') as f:
    f.write(read_data)
