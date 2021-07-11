/* Modified the llvm-hello_world program provided with Phasar
to iterate through functions in the module and count the number of loops. */

#include <cxxabi.h>
#include <iostream>
#include <llvm/IR/CallSite.h>
#include <llvm/IR/Constants.h>
#include <llvm/IR/DebugLoc.h>
#include <llvm/IR/Function.h>
#include <llvm/IR/Instruction.h>
#include <llvm/IR/Instructions.h>
#include <llvm/IR/IntrinsicInst.h>
#include <llvm/IR/LLVMContext.h>
#include <llvm/IR/Module.h>
#include <llvm/IR/Verifier.h>
#include <llvm/IRReader/IRReader.h>
#include <llvm/IR/Dominators.h>
#include <llvm/Support/SMLoc.h>
#include <llvm/Support/SourceMgr.h>
#include <llvm/Support/raw_ostream.h>
#include <llvm/IR/DerivedTypes.h>
#include <memory>
#include <string>
#include <vector>
//GLOBALS

int NUM_INST = 45;
// Prototypes
int countLoops(std::unique_ptr<llvm::Module>&);
int countVars(std::unique_ptr<llvm::Module>&);
int countIfs(std::unique_ptr<llvm::Module>&);
int countFuncs(std::unique_ptr<llvm::Module>&);
int countCalls(std::unique_ptr<llvm::Module>&);
int countFundamentalTypes(std::unique_ptr<llvm::Module>&);
int countArrayTypes(std::unique_ptr<llvm::Module>&);
int countCompositeTypes(std::unique_ptr<llvm::Module>&);
int countPointerTypes(std::unique_ptr<llvm::Module>&);
int countLLVMLines(std::unique_ptr<llvm::Module>&);
int countLoads(std::unique_ptr<llvm::Module>&);
int countStores(std::unique_ptr<llvm::Module>&);
std::vector<int> countInstructions(std::unique_ptr<llvm::Module>&);

bool isFunctionValid(llvm::Function&);
bool isFunctionEmpty(llvm::Function&);
  
llvm::BasicBlock* backEdge(const llvm::BasicBlock*, llvm::DominatorTree*);

std::string cxx_demangle(const std::string &mangled_name) {
  int status = 0;
  char *demangled =
      abi::__cxa_demangle(mangled_name.c_str(), NULL, NULL, &status);
  std::string result((status == 0 && demangled != NULL) ? demangled
                                                        : mangled_name);
  free(demangled);
  return result;
}

int main(int argc, char **argv) {
  
  if (argc != 2) {
    std::cout << "usage: ./extractFeatures <IR file>\n";
    return 1;
  }
  // parse an IR file into an LLVM module
  llvm::SMDiagnostic Diag;
  std::unique_ptr<llvm::LLVMContext> C(new llvm::LLVMContext);
  std::unique_ptr<llvm::Module> M = llvm::parseIRFile(argv[1], Diag, *C);
  
  // check if the module is alright
  bool broken_debug_info = false;
  if (M.get() == nullptr) {
    llvm::errs() << "error: module is null\n";
    llvm::errs() << Diag.getMessage() << "\n";
  }
  if (llvm::verifyModule(*M, &llvm::errs(), &broken_debug_info)) {
    llvm::errs() << "error: module not valid\n";
    return 1;
  }
  if (broken_debug_info) {
    llvm::errs() << "caution: debug info is broken\n";
  }
  /* Get rid of this because I want to iterate through every function, not just main */
  // auto F = M->getFunction("main");
  // if (!F) {
  //   llvm::errs() << "error: could not find function 'main'\n";
  //   return 1;
  // }

  // Compute features, output
  llvm::outs() << argv[1] << ",";
  int numVars = countVars(M);
  llvm::outs() << numVars << ",";
  // Currently, numIfs includes conditional branches that are part of loop headers.
  //  To change this, simply subtract 
  int numIfs = countIfs(M);
  llvm::outs() << numIfs << ",";
  int numLoops = countLoops(M);
  llvm::outs() << numLoops << ",";
  int numFuncs = countFuncs(M);
  llvm::outs() << numFuncs << ",";
  int numCalls = countCalls(M);
  llvm::outs() << numCalls << ",";
  int numFundTypes = countFundamentalTypes(M);
  llvm::outs() << numFundTypes << ",";
  int numArrayTypes = countArrayTypes(M);
  llvm::outs() << numArrayTypes << ",";
  int numPointerTypes = countPointerTypes(M);
  llvm::outs() << numPointerTypes << ",";
  int numCompTypes = countCompositeTypes(M);
  llvm::outs() << numCompTypes << ",";
  /* numThings is numVars (which is countFundamentalTypes + countPointerTypes +
     countCompositeTypes + countArrayTyes) + numIfs (which includes numLoops) + 
     numFuncs + numCalls + numLoads + numStores */
  int numLoads = countLoads(M);
  int numStores = countStores(M);
  llvm::outs() << numVars + numIfs + numFuncs + numCalls + numLoads + numStores<< ",";
  int numLines = countLLVMLines(M);
  llvm::outs() << numLines << ",";
  llvm::outs() << numLoads << ",";
  llvm::outs() << numStores << ",";
  std::vector<int> instructions = countInstructions(M);
  for (int i = 0; i < NUM_INST; i++) {
    llvm::outs() << instructions[i];
    if (i < (NUM_INST-1)) llvm::outs() << ",";
  }
  llvm::outs() << "\n";
  llvm::llvm_shutdown();
  return 0;
}

int countLoops(std::unique_ptr<llvm::Module> & M) {
  int loopCount = 0;
 for (llvm::Function &F: *M) {

   if (!isFunctionValid(F)) {
     llvm::errs() << "error: function " << cxx_demangle(F.getName()) << "  not valid, skipping\n";
      continue;
    }

    // Check if the function has zero lines
    if (isFunctionEmpty(F)) {
      llvm::errs() << "This function " << cxx_demangle(F.getName()) << " has no instructions; skipping.\n";
      continue;
    }
    

    // Get the dominator tree
    llvm::DominatorTree* domTree = new llvm::DominatorTree(F);

    // Set up a list of loop headers.
    std::vector<llvm::BasicBlock*>* loopHeaders = new std::vector<llvm::BasicBlock*>();

    // Iterate through the basic blocks
    for (const auto &BB: F) {
      llvm::BasicBlock* header = backEdge(&BB, domTree);
      if (header) {
	loopHeaders->push_back(header);
      }
    }
    loopCount += loopHeaders->size();
 }
 return loopCount;
}

bool isFunctionValid(llvm::Function &F) {
  return !llvm::verifyFunction(F, &llvm::errs());
}

bool isFunctionEmpty(llvm::Function &F) {
  return F.begin() == F.end();
}
  
int countVars(std::unique_ptr<llvm::Module> &M) {
  // This counts the number of variables in the program.
  // I do this by counting the number of alloc instructions
  int varCount = 0;
  for (llvm::Function &F: *M) {
    if (!isFunctionValid(F)) {
      llvm::errs() << "This function " << cxx_demangle(F.getName()) << " is invalid; skipping.\n";
      continue;
    }
    if (isFunctionEmpty(F)) {
      llvm::errs() << "The function " << cxx_demangle(F.getName()) << " is invalid; skipping.\n";
      continue;
    }

    for (auto &BB: F) {
      for (auto &I: BB) {
	if (auto alloc = llvm::dyn_cast<llvm::AllocaInst>(&I)) {
	  varCount++;
	}
      }
    }
  }
  return varCount;
}

int countIfs(std::unique_ptr<llvm::Module> &M) {
  // Iterate through functions
  int ifCount = 0;
  for (llvm::Function &F: *M) {
    if (!isFunctionValid(F)) {
      llvm::errs() << "This function " << cxx_demangle(F.getName()) << " is invalid; skipping.\n";
      continue;
    }
    if (isFunctionEmpty(F)) {
      llvm::errs() << "This function " << cxx_demangle(F.getName()) << " is empty; skipping.\n";
      continue;
    }

    for (llvm::BasicBlock &BB: F) {
      for (llvm::Instruction &I: BB) {

	// Check if the instruction is a branch
	if (auto Branch = llvm::dyn_cast<llvm::BranchInst>(&I)) {
	  // Only count if this is a conditional branch
	  if (Branch->isConditional()) ifCount += 1;
	}
      }
    }
  }

  return ifCount;

}

llvm::BasicBlock* backEdge(const llvm::BasicBlock* node, llvm::DominatorTree* domTree) {
  /* Checks whether the node has back edges coming from it.
     Does this by checking whether any of the node's successors are also dominators.
     If it has a back edge, return the BasicBlock that the back edge points to.
     Otherwise, return null */

  const llvm::TerminatorInst* terminator = node->getTerminator();
  for (unsigned int i = 0; i < terminator->getNumSuccessors(); i++) {
    llvm::BasicBlock* successor = terminator->getSuccessor(i);
    if (domTree->properlyDominates(successor, node)) {
      return successor;
    }
  }
  return nullptr;
}

int countFuncs(std::unique_ptr<llvm::Module>& M) {
  int numFuncs = 0;
  for (llvm::Function &F: *M) {
    if (!isFunctionValid(F)) {
      llvm::errs() << "Function " << cxx_demangle(F.getName()) << " invalid; skipping.\n";
      continue;
    }

    if (isFunctionEmpty(F)) {
      llvm::errs() << "Function " << cxx_demangle(F.getName()) << " is empty; skipping.\n";
      continue;
    }

    numFuncs += 1;
  }
  return numFuncs;
}

int countCalls(std::unique_ptr<llvm::Module>& M) {
  int numCalls = 0;
  for (llvm::Function &F: *M) {
    if (!isFunctionValid(F)) {
      llvm::errs() << "Function " << cxx_demangle(F.getName()) << " invalid; skipping.\n";
      continue;
    }
    if (isFunctionEmpty(F)) {
      llvm::errs() << "Function " << cxx_demangle(F.getName()) << " is empty; skipping.\n";
      continue;
    }

    for (auto &BB: F) {
      for (auto &I: BB) {

	// Calls can be either callinst or invokeinst
	if (auto CallI = llvm::dyn_cast<llvm::CallInst>(&I)) {
	  numCalls += 1;
	}
	if (auto InvokeI = llvm::dyn_cast<llvm::InvokeInst>(&I)) {
	  numCalls += 1;
	}
	  
      }
    }
  }
  return numCalls;
}

int countFundamentalTypes(std::unique_ptr<llvm::Module>& M) {
  // This counts the number of variables in the program.
  // I do this by counting the number of alloc instructions
  int fundCount = 0;
  for (llvm::Function &F: *M) {
    if (!isFunctionValid(F)) {
      llvm::errs() << "This function " << cxx_demangle(F.getName()) << " is invalid; skipping.\n";
      continue;
    }
    if (isFunctionEmpty(F)) {
      llvm::errs() << "Function " << cxx_demangle(F.getName()) << " is empty; skipping.\n";
      continue;
    }


    for (auto &BB: F) {
      for (auto &I: BB) {

	if (auto alloc = llvm::dyn_cast<llvm::AllocaInst>(&I)) {
	  llvm::Type::TypeID tid = alloc->getAllocatedType()->getTypeID();
	  switch (tid) {
	    // blacklist: Non fundamental types
	    // full type list is at https://llvm.org/doxygen/classllvm_1_1Type.html#a5e9e1c0dd93557be1b4ad72860f3cbdaa2989d3024a84b4dda9d77419b1648554
	  case llvm::Type::TypeID::LabelTyID:
	  case llvm::Type::TypeID::MetadataTyID:
	  case llvm::Type::TypeID::TokenTyID:
	  case llvm::Type::TypeID::FunctionTyID:
	  case llvm::Type::TypeID::StructTyID:
	  case llvm::Type::TypeID::ArrayTyID:
	  case llvm::Type::TypeID::PointerTyID:
	  case llvm::Type::TypeID::VectorTyID:
	    break;
	  default:
	    fundCount++;
	  }
	}
      }
    }
  }
  return fundCount;
}

int countArrayTypes(std::unique_ptr<llvm::Module>& M) {
  // This counts the number of variables in the program.
  // I do this by counting the number of alloc instructions
  int arrayCount = 0;
  for (llvm::Function &F: *M) {
    if (!isFunctionValid(F)) {
      llvm::errs() << "This function " << cxx_demangle(F.getName()) << " is invalid; skipping.\n";
      continue;
    }
    if (isFunctionEmpty(F)) {
      llvm::errs() << "Function " << cxx_demangle(F.getName()) << " is empty; skipping.\n";
      continue;
    }


    for (auto &BB: F) {
      for (auto &I: BB) {

	if (auto alloc = llvm::dyn_cast<llvm::AllocaInst>(&I)) {
	  llvm::Type::TypeID tid = alloc->getAllocatedType()->getTypeID();

	  switch (tid) {
	  case llvm::Type::TypeID::ArrayTyID:
	    arrayCount++;
	    break;
	  default:
	    break;
	  }
	}
      }
    }
  }
  return arrayCount;
}
		      
int countPointerTypes(std::unique_ptr<llvm::Module>& M) {
  // This counts the number of variables in the program.
  // I do this by counting the number of alloc instructions
  int pointerCount = 0;
  for (llvm::Function &F: *M) {
    if (!isFunctionValid(F)) {
      llvm::errs() << "This function is invalid; skipping.\n";
      continue;
    }
    if (isFunctionEmpty(F)) {
      llvm::errs() << "Function " << cxx_demangle(F.getName()) << " is empty; skipping.\n";
      continue;
    }


    for (auto &BB: F) {
      for (auto &I: BB) {

	if (auto alloc = llvm::dyn_cast<llvm::AllocaInst>(&I)) {
	  llvm::Type::TypeID tid = alloc->getAllocatedType()->getTypeID();

	  switch (tid) {
	  case llvm::Type::TypeID::PointerTyID:
	    pointerCount++;
	    break;
	  default:
	    break;
	  }
	}
      }
    }
  }
  return pointerCount;
}
int countCompositeTypes(std::unique_ptr<llvm::Module>& M) {
  // This counts the number of variables in the program.
  // I do this by counting the number of alloc instructions
  int compositeCount = 0;
  for (llvm::Function &F: *M) {
    if (!isFunctionValid(F)) {
      llvm::errs() << "This function " << cxx_demangle(F.getName()) << " is invalid; skipping.\n";
      continue;
    }
    if (isFunctionEmpty(F)) {
      llvm::errs() << "Function " << cxx_demangle(F.getName()) << " is empty; skipping.\n";
      continue;
    }
    for (auto &BB: F) {
      for (auto &I: BB) {

	if (auto alloc = llvm::dyn_cast<llvm::AllocaInst>(&I)) {
	  llvm::Type::TypeID tid = alloc->getAllocatedType()->getTypeID();
	  switch (tid) {
	  case llvm::Type::TypeID::StructTyID:
	    compositeCount++;
	    break;
	  default:
	    break;
	  }
	}
      }
    }
  }
  return compositeCount;
}

int countLLVMLines(std::unique_ptr<llvm::Module>& M) {
  int lineCount = 0;
  for (llvm::Function &F: *M) {
    if (!isFunctionValid(F)) {
      llvm::errs() << "This function " << cxx_demangle(F.getName()) << " is invalid; skipping.\n";
      continue;
    }
    if (isFunctionEmpty(F)) {
      llvm::errs() << "Function " << cxx_demangle(F.getName()) << " is empty; skipping.\n";
      continue;
    }

    for (auto &BB: F) {
      for (auto &I: BB) {
	lineCount++;
      }
    }
  }
  return lineCount;
}

int countLoads(std::unique_ptr<llvm::Module>& M) {
  int loadCount = 0;
  for (llvm::Function &F: *M) {
    if (!isFunctionValid(F)) {
      llvm::errs() << "This function " << cxx_demangle(F.getName()) << " is invalid; skipping.\n";
      continue;
    }
    if (isFunctionEmpty(F)) {
      llvm::errs() << "Function " << cxx_demangle(F.getName()) << " is empty; skipping.\n";
      continue;
    }

    for (auto &BB: F) {
      for (auto &I: BB) {

	if (auto Load = llvm::dyn_cast<llvm::LoadInst>(&I)) {
	  loadCount++;
	}
      }
    }
  }
  return loadCount;
}

int countStores(std::unique_ptr<llvm::Module>& M) {
  int storeCount = 0;
  for (llvm::Function &F: *M) {
    if (!isFunctionValid(F)) {
      llvm::errs() << "This function " << cxx_demangle(F.getName()) << " is invalid; skipping.\n";
      continue;
    }
    if (isFunctionEmpty(F)) {
      llvm::errs() << "Function " << cxx_demangle(F.getName()) << " is empty; skipping.\n";
      continue;
    }

    for (auto &BB: F) {
      for (auto &I: BB) {

	if (auto Store = llvm::dyn_cast<llvm::StoreInst>(&I)) {
	  storeCount++;
	}
      }
    }
  }
  return storeCount;
}

std::vector<int> countInstructions(std::unique_ptr<llvm::Module>& M) {
  std::vector<int> instCounts(NUM_INST,0);
  for (llvm::Function &F: *M) {
    if (!isFunctionValid(F)) {
      llvm::errs() << "This function " << cxx_demangle(F.getName()) << " is invalid; skipping.\n";
      continue;
    }
    if (isFunctionEmpty(F)) {
      llvm::errs() << "Function " << cxx_demangle(F.getName()) << " is empty; skipping.\n";
      continue;
    }

    for (auto &BB: F) {
      for (auto &I: BB) {
	int i = 0;
	if ( llvm::dyn_cast<llvm::AddrSpaceCastInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::AllocaInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::AtomicCmpXchgInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::AtomicRMWInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::BitCastInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::BranchInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::CallInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::CatchPadInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::CatchReturnInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::CatchSwitchInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::CleanupPadInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::CleanupReturnInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::ExtractElementInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::ExtractValueInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::FCmpInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::FenceInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::FPExtInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::FPToSIInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::FPToUIInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::FPTruncInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::GetElementPtrInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::ICmpInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::IndirectBrInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::InsertElementInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::InsertValueInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::IntToPtrInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::InvokeInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::LandingPadInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::LoadInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::PHINode>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::PtrToIntInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::ResumeInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::ReturnInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::SelectInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::SExtInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::ShuffleVectorInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::SIToFPInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::StoreInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::SwitchInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::TruncInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::UIToFPInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::UnreachableInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::VAArgInst>(&I))
	  instCounts[i]++;
	else if (i++ &&  llvm::dyn_cast<llvm::ZExtInst>(&I))
	  instCounts[i]++;
      }
    }
  }
  return instCounts;
}

