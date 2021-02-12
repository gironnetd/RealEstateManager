# Got these colors from a medium article (I can't remember author)
# Functions for customizing colors(Optional)
print_green(){
    printf "\e[1;32m$1\e[0m"
}
print_blue(){
    printf "\e[1;34m$1\e[0m"
}

cd ..

print_blue "\n run androidTests...\n"
./gradlew -Pandroid.testInstrumentationRunnerArguments.class=com.openclassrooms.realestatemanager.data.remote.PropertyApiServiceTest connectedAndroidTest
print_green "\n androidTests COMPLETE.\n"

print_green "\n Done.\n"













