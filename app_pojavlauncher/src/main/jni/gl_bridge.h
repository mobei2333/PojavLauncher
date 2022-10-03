//
// Created by maks on 17.09.2022.
//

#ifndef POJAVLAUNCHER_GL_BRIDGE_H
#define POJAVLAUNCHER_GL_BRIDGE_H

typedef struct {
    char       state;
    EGLConfig  config;
    EGLint     format;
    EGLContext context;
    EGLSurface surface;
    struct ANativeWindow *nativeSurface;
    struct ANativeWindow *newNativeSurface;
} render_bundle_t;

bool gl_init();
render_bundle_t* gl_init_context(render_bundle_t* share);
void gl_make_current(render_bundle_t* bundle);
void gl_swap_buffers();
void gl_setup_window(struct ANativeWindow* window);
void gl_swap_interval(int swapInterval);


#endif //POJAVLAUNCHER_GL_BRIDGE_H
