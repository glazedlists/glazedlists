 M4 Macros

m4_divert(-1)
# forloop(i, from, to, stmt)
m4_define(`forloop', `m4_pushdef(`$1', `$2')_forloop(`$1', `$2', `$3', `$4')m4_popdef(`$1')')
m4_define(`_forloop',
       `$4`'m4_ifelse($1, `$3', ,
             `m4_define(`$1', m4_incr($1))_forloop(`$1', `$2', `$3', `$4')')')
m4_divert

m4_define(`VAR_LAST_COLOR_INDEX', `m4_eval(VAR_COLOUR_COUNT-1)')
m4_define(`originalCounti', ``originalCount'indexToBit($1)')
m4_define(`indexToBit', `m4_eval(`2 ** $1')')
m4_define(`counti', ``count'indexToBit($1)')

m4_define(COLORED_START, `m4_ifelse(VAR_COLOUR_COUNT,1,$1,')
m4_define(COLORED_END, `)')

m4_define(WIDE_NODES_START, `m4_ifelse(VAR_WIDE_NODES,false,$1,')
m4_define(WIDE_NODES_END, `)')

# define a function NODE_WIDTH(boolean) to get the node's size for this color
m4_ifelse(VAR_WIDE_NODES,true,`
    m4_define(NODE_WIDTH, size)
',`
    m4_define(NODE_WIDTH, $1 ? 1 : 0)
')

# define a function NODE_SIZE(node, colors) to no node.nodeSize()
m4_ifelse(VAR_WIDE_NODES,true,`
    m4_ifelse(VAR_COLOR_COUNT,1,`
       m4_define(NODE_SIZE,$1.size)
    ',`
       m4_define(NODE_SIZE,$1.nodeSize($2))
    ')
',`
   m4_define(NODE_SIZE, 1)
')

# define a function to refresh counts
m4_ifelse(VAR_WIDE_NODES,false,`
   m4_define(REFRESH_COUNTS,$1.refreshCounts(!zeroQueue.contains($1)); `EXAMPLE_START')
',`
   m4_define(REFRESH_COUNTS,$1.refreshCounts(); `EXAMPLE_START')
')

