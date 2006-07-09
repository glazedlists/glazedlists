 M4 Macros

STANDARD M4 LOOP ---------------------------------------------------------------

m4_divert(-1)
# forloop(i, from, to, stmt)

m4_define(`forloop', `m4_pushdef(`$1', `$2')_forloop(`$1', `$2', `$3', `$4')m4_popdef(`$1')')
m4_define(`_forloop',
       `$4`'m4_ifelse($1, `$3', ,
             `m4_define(`$1', m4_incr($1))_forloop(`$1', `$2', `$3', `$4')')')
m4_divert

MACRO CODE WITH A JAVA ALTERNATIVE ---------------------------------------------
m4_define(`BEGIN_M4_MACRO', ` BEGIN M4 MACRO GENERATED CODE *'`/')
m4_define(`END_M4_MACRO', `/'`* END M4 MACRO GENERATED CODE ')
m4_define(`BEGIN_M4_ALTERNATE', `BEGIN M4 ALTERNATE CODE
/'`* ')
m4_define(`END_M4_ALTERNATE', `END ALTERNATE CODE *'`/')

NODE SPECIFIC VARIABLES & FUNCTIONS--- -----------------------------------------
m4_define(`VAR_LAST_COLOR_INDEX', `m4_eval(VAR_COLOUR_COUNT-1)')
m4_define(`originalCounti', ``originalCount'indexToBit($1)')
m4_define(`indexToBit', `m4_eval(`2 ** $1')')
m4_define(`NodeN', `Node'VAR_COLOUR_COUNT)
m4_define(`TreeN', `Tree'VAR_COLOUR_COUNT)
m4_define(`TreeNAsList', ``Tree'VAR_COLOUR_COUNT`AsList'')
m4_define(`TreeNIterator', ``Tree'VAR_COLOUR_COUNT`Iterator'')
m4_define(`counti', ``count'indexToBit($1)')

USE ALTERNATE CODE WHEN WE ONLY HAVE ONE COLOR ---------------------------------
m4_define(`SINGLE_ALTERNATE', m4_ifelse(VAR_COLOUR_COUNT,`1',`USE SINGLE ALTERNATE *'`/ '$1`
// IGNORE DEFAULT:',`USE DEFAULT'))
m4_define(`END_SINGLE_ALTERNATE', m4_ifelse(VAR_COLOUR_COUNT,`1',`
/'`* END SINGLE ALTERNATE',`END DEFAULT'))

SKIP SECTIONS OF CODE WHEN WE ONLY HAVE ONE COLOR ------------------------------
m4_define(`BEGIN_SINGLE_SKIP', m4_ifelse(VAR_COLOUR_COUNT,`1',`
/'`* BEGIN SINGLE SKIPPED CODE '))
m4_define(`END_SINGLE_SKIP', m4_ifelse(VAR_COLOUR_COUNT,`1',`END SINGLE SKIPPED CODE *'`/'))


m4_define(COLORED_START, `m4_ifelse(VAR_COLOUR_COUNT,1,$1,')
m4_define(COLORED_END, `)')
