#define TYPE_CHECK(T, S)                                     \
    while (false) {                                              \
      *(static_cast<T* volatile*>(0)) = static_cast<S*>(0);      \
    }