# some M4 Macros that make it easy to use m4 with Java
m4_define(QUOTE_STOP, `/*[')
m4_define(QUOTE_START, `]*'`/')
m4_define(EXAMPLE_START, `m4_ifelse(')
m4_define(EXAMPLE_END, `)')
m4_define(BEGIN_M4_JAVA, ``BEGIN_M4_JAVA' QUOTE_START m4_changequote(QUOTE_START, QUOTE_STOP) ')
m4_define(END_M4_JAVA, `m4_changequote QUOTE_STOP `END_M4_JAVA'')
m4_define(GENERATED_CODE_START, `m4_changequote')
m4_define(GENERATED_CODE_END, `m4_changequote(QUOTE_START, QUOTE_STOP)')

