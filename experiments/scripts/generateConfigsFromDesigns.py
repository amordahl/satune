#!/usr/bin/env python
"""
Generates executable configurations that can be passed to the verfiers.
Consumes 
	1. the configuration space file which defines the list/set of configuration options
and their settings that were used in the study.
	2. The covering array as a csv file. This covering array file is the set of samples 
	used in the ground-truth dataset generation study.
Writes the configurations in `Configs-Xopts-t3-CA.txt` file under each tool directory.
"""
import re,os
import Utils_Config as cu

HOME_DIR = str(os.getenv("HOME"))
BASE_DIR=f'{HOME_DIR}/satune/experiments'

# tools dictionary {tool: [configuration_space_file, covering_array_file, save_file]}
tools={'cbmc': ['Config-space-21opts.txt','CA-21opts-t3.csv', 'Configs-21opts-t3-CA.txt'],
			 'jayhorn': ['Config-space-12opts.txt','CA-12opts-t3.csv', 'Configs-12opts-t3-CA.txt'], 
			 'jbmc': ['Config-space-27opts.txt', 'CA-27opts-t3.csv', 'Configs-27opts-t3-CA.txt'], 
			 'symbiotic': ['Config-space-16opts.txt','CA-16opts-t3.csv', 'Configs-16opts-t3-CA.txt']}

for tool, fields in tools.items():
	print(f'Processing configurations of {tool} with {fields}')
	toolPath = f'{BASE_DIR}/tools/{tool}'
	
	domFile = open(f'{toolPath}/{fields[0]}', 'r')
	options = [opt.split(':')[0] for opt in domFile.readlines()]
	domFile.close()

	designFile = open(f'{toolPath}/{fields[1]}','r')
	lines = designFile.readlines()
	designFile.close()
	with open(f'{toolPath}/{fields[2]}','w') as pretyConfigs:
		for line in lines:
			pretyConfigs.write(cu.designLine2Config(tool, line, options) + '\n')
		print(f'Wrote {len(lines)} configurattions to {fields[2]} file for {tool}.')
	