//
// Created by admin on 2018/5/24.
//
#include "cgeGLFunctions.h"

namespace CGE {
    class CGEWaterMark
    {
    public:
        CGEWaterMark();
        ~CGEWaterMark();

        void setBitmap(long *map);

        void init();

    protected:
        GLint m_TextureId;
    };
}