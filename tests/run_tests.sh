print_blue(){
    printf "\e[1;34m$1\e[0m"
}

print_blue "\n\n\nStarting Firestore Local Emulator...\n"
lsof -t -i tcp:8080 | xargs kill
firebase emulators:exec "./ui_tests.sh"
















