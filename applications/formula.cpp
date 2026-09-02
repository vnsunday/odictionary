#include <stdio.h>
#include <stdlib.h>

void quality_form() 
{
    const char* azPosition1[][2] = {
        { "Before", "Noun" },
        { "After", "Verb" },
        { "After", "Noun" }
    };

    const char* szQualityAware[][2] = {
        { }
    };
    int n1 = sizeof(azPosition1)/sizeof(azPosition1[0]);
}

void additional_meaning() {
    // Obsevation 
    const char* azAM[][2] = {
        { "Additional", "Word" }, 
        { "Word", "Additional" }, 
        { "Compound", "Additional" }, 
        { "Addtional", "Compound" }
    };

    const char* szKinds[] = {
        "Manner", "Time", "Place", "Degree", "Quantity", "Interrogative", "Relative"
    };
    // PP - a component of reduced clause
}

void quality_exception() {
    const char* azList[] = {
        "present"
    };
}

int main()
{
    return 0;
}