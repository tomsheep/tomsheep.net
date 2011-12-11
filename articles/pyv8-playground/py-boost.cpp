#include <boost/python.hpp>
using namespace boost::python;

std::string foo_func()
{
      return std::string("Hello, World");
}

std::string bar_func(const std::string& str, int n)
{
      std::string tmp;
      for(int i = 0; i < n; ++i)
      {
            tmp += str;
      }
      return tmp;
}

class foo_class
{
private:
      std::string foo;
public:
      static std::string bar;
      foo_class():foo("I am foo"){}
      std::string f(int n = 1)
      {
            std::string tmp;
            for(int i = 0; i < n; i++)
            {
                  tmp += foo;
            }
            return tmp;
      }

};

std::string foo_class::bar = "I am bar";

//定义名为boostpy的python模块
BOOST_PYTHON_MODULE(boostpy)
{
      // 导出foo和bar两个模块全局函数
      def("foo", foo_func, "foo function");
      def("bar", bar_func, (arg("str"), "n"));

      // 导出foo_class类，类中选择性导出f成员方法和s_bar静态变量
      class_<foo_class>("foo_class", "I have nothing to say")
            .def("f", &foo_class::f, arg("n")=1)
            .def_readwrite("s_bar", &foo_class::bar);
}