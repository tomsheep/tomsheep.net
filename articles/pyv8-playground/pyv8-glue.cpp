CContext::CContext(py::object global, py::list extensions)
{
	v8::HandleScope handle_scope;  

	std::auto_ptr<v8::ExtensionConfiguration> cfg;
	std::vector<std::string> ext_names;
	std::vector<const char *> ext_ptrs;

	for (Py_ssize_t i=0; i<PyList_Size(extensions.ptr()); i++)
	{
		py::extract<const std::string> extractor(
			::PyList_GetItem(extensions.ptr(), i));

		if (extractor.check())
		{
			ext_names.push_back(extractor());
		}
	}

	for (size_t i=0; i<ext_names.size(); i++)
	{
		ext_ptrs.push_back(ext_names[i].c_str());
	}

	if (!ext_ptrs.empty()) cfg.reset(new v8::ExtensionConfiguration
		(ext_ptrs.size(), &ext_ptrs[0]));

	m_context = v8::Context::New(cfg.get());

	v8::Context::Scope context_scope(m_context);

	if (global.ptr() != Py_None)
	 {
		m_context->Global()->Set(v8::String::NewSymbol
			("__proto__"), CPythonObject::Wrap(global));
	}
}