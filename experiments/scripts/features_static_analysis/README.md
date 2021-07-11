# Building the tool

You can build the tool with the provided makefile; simply run "make". You must have clang++ installed and on your PATH.

# Using the tool

You can run the tool as follows:

`./extractFeatures <LLVM IR FILE> 2>/dev/null`

The redirection of STDERR is to ensure that no debug or error information appears in the results.

Note that this tool works on LLVM IR and not on .c or .cpp files. The tool only runs on a single file, so use something like find or xargs to run it in batch. To compile to IR, you can run the following command:

`clang -S -emit-llvm <SOURCE FILE>`

For .cpp files, replace clang with clang++.

# Output format

fileName,numVars,numIfs,numLoops,numFuncs,numCalls,numFundTypes,numArrayTypes,numPointerTypes,numCompositeTypes,numThings,numLines,numLoads,numStores

Following this are counts of the following instruction types:
AddrSpaceCastInst
AllocaInst
AtomicCmpXchgInst
AtomicRMWInst
BitCastInst
BranchInst
CallInst
CaseIteratorImpl
CatchPadInst
CatchReturnInst
CatchSwitchInst
CleanupPadInst
CleanupReturnInst
ExtractElementInst
ExtractValueInst
FCmpIns
FenceInst
FPExtInst
FPToSIInst 
FPToUIInst 
FPTruncInst
GetElementPtrInst
ICmpIns
IndirectBrInst
InsertElementInst
InsertValueInst
IntToPtrInst
InvokeInst
LandingPadInst
LoadInst
PHINode
PtrToIntInst
ResumeInst
ReturnInst
SelectInst
SExtInst
ShuffleVectorInst
SIToFPInst
StoreInst
SwitchInst
TruncInst
UIToFPInst
UnreachableInst
VAArgInst
ZExtInst
