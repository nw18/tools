//
// Created by Administrator on 2017/3/24.
//

#ifndef MUTUALHARM_HARM_H_H
#define MUTUALHARM_HARM_H_H

#include "Box2D/Box2D.h"

class HarmWorld {
private:
    HarmWorld();
    b2World mWorld;

public:
    ~HarmWorld();
    void setup();
};

#endif //MUTUALHARM_HARM_H_H
