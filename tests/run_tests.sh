print_blue(){
    printf "\e[1;34m$1\e[0m"
}

print_blue "\nStarting Firestore Local Emulator...\n"
lsof -t -i tcp:8080 | xargs kill
export FIRESTORE_EMULATOR_HOST=localhost:8080
firebase emulators:exec "./firestore_emulator_tests.sh"
















