import random,itertools

def designLine2Config(tool, line, options):
	index = -1
	runConfig = ''
	values = line.strip().split(',')
	if 'cbmc' == tool:
		for v in values:
			index += 1
			if v == '' or v == 'off' or  v == '\n' or v=='0' or v=='0\n':
				continue
			elif v == 'on' or (v == '1' and index!=12): # for --unwind 1 12 for cbmc 18 for jbmc
				runConfig += options[index] + ' '
			elif v.startswith('--'):
				runConfig += v + ' '
			else:
				runConfig += options[index] + ' ' + v + ' '
	elif 'jbmc' == tool:
		for v in values:
			index += 1
			if v == '' or v == 'off' or  v == '\n' or v=='0' or v=='0\n':
				continue
			elif v == 'on' or (v == '1' and index!=18): # for --unwind 1 12 for cbmc 18 for jbmc
				runConfig += options[index] + ' '
			elif v.startswith('--'):
				runConfig += v + ' '
			else:
				runConfig += options[index] + ' ' + v + ' '
	elif 'jayhorn' == tool:
		for v in values:
			index += 1
			if v == '' or v == 'off' or  v == '\n' or v=='0' or v=='0\n':
				continue
			elif v == 'on':
				runConfig += options[index] + ' '
			else:
				runConfig += options[index] + ' ' + v + ' '
	elif 'symbiotic' == tool:
		optimize = ''
		values = line.strip().split(',')
		for v in values:
			index += 1
			if (index==14 or index==15):
				if v !='off': #symbiotic specific
					optimize += v+':' # TODO: Change : -> , before using the configs file for running the verification tools
			elif v == '' or v == 'off' or  v == '\n' or v=='0' or v=='0\n':
				continue
			elif v == 'on':
				runConfig += options[index] + ' '
			elif v.startswith('--'):
				runConfig += v + ' '
			else:
				runConfig += options[index] + ' ' + v + ' '
		if optimize=='':
			runConfig+='--no-optimize'
		else:
			runConfig+='--optimize='+optimize[:-1]
	return runConfig.strip()

def readDomFile(domFile):
	domFile = open(domFile, 'r')
	optionLines = domFile.read().splitlines()
	domFile.close()

	optionNames = []
	optionSettings = []
	for opt in optionLines:
		parts = opt.split(':')
		optionNames.append(parts[0])
		optionSettings.append(parts[1].split(','))
	return optionNames, optionSettings

# Following function are not in-use!
def readConfigsFile(configsFile):
	configsFile = open(configsFile, 'r')
	configs = [line.split(',') for line in configsFile.read().splitlines()]
	configsFile.close()
	return configs

def useCoveringArray(t=2, optionSettings=None):
	configsFile='../designs/CBMB-t'+str(t)+'-CA.txt'
	configsFile = open(configsFile, 'r')
	configs = [line.split(',') for line in configsFile.read().splitlines()]
	configsFile.close()
	return configs

def generateRandomConfigs(count=100, optionSettings=None):
	configs=[]
	for config in itertools.product(*optionSettings):
		configs.append(list(config))
		if len(configs)>=count:
			break
	return configs

def enumarateNConfigs(count=100, optionSettings=None):
	configs=[]
	for x in xrange(0,count):
		config=[]
		for optSettings in optionSettings:
			indx=random.randint(0, len(optSettings)-1)
			config.append(optSettings[indx])
		configs.append(config)
	return configs
