/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.text;

/**
 * Latin characters are mapped to strip their diacritics from each character
 * according to the rules of Unicode. For an introduction to unicode
 * normalization, go <a href="http://www.unicode.org/reports/tr15/">here</a>.
 *
 * <p>The complete set of encoded Latin characters within Unicode looks like so:
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr><td class="TableSubHeadingColor">Basic Latin and Control Characters</td><td>0x0000 -> 0x007F (0 -> 127)</td></tr>
 * <tr><td class="TableSubHeadingColor">Latin-1 Supplement and C1 Controls</td><td>0x0080 -> 0x00FF (128 -> 255)</td></tr>
 * <tr><td class="TableSubHeadingColor">Latin Extended-A</td><td>0x0100 -> 0x017F (256 -> 372)</td></tr>
 * <tr><td class="TableSubHeadingColor">Latin Extended-B</td><td>0x0180 -> 0x024F (384 -> 591)</td></tr>
 * <tr><td class="TableSubHeadingColor">Latin Extended Additional</td><td>0x1E00 -> 0x1EFF (7680 -> 7935)</td></tr>
 * <tr><td class="TableSubHeadingColor">Latin Extended-C</td><td>0x2C60 -> 0x2C7F (11360 -> 11391)</td></tr>
 * <tr><td class="TableSubHeadingColor">Latin Extended-D</td><td>0xA720 -> 0xA7FF (42784 -> 43007)</td></tr>
 * <tr><td class="TableSubHeadingColor">Latin Ligatures</td><td>0xFB00 -> 0xFB4F (64256 -> 64335)</td></tr>
 * <tr><td class="TableSubHeadingColor">Small Forms</td><td>0xFE50 -> 0xFE6F (65104 -> 65135)</td></tr>
 * <tr><td class="TableSubHeadingColor">Full-Width Latin Letters</td><td>0xFF00 -> 0xFFEF (65280 -> 65519)</td></tr>
 * </table>
 *
 * This LatinDiacriticsStripper only maps the first 4 sets (0 -> 591), since
 * the rest are quite fringe and extremely rare in practice.
 *
 * <p>For more details see the <a href="http://www.unicode.org/charts/">Unicode Charts</a>.
 *
 * @author James Lemieux
 */
public final class LatinDiacriticsStripper {

    /** The static map initialized when the class is loaded. */
    private static final char[] MAPPER = new char[592];

    /**
     * This method strips diacritics from latin characters, which allows fuzzy
     * matching between languages. For example, the mapped value of é is e.
     * So, the word "résumé" could be matched by simply typing "resume".
     *
     * @return the normalized version of <code>c</code>, which can be any character
     */
    public static char[] getMapper() {
        return MAPPER;
    }

    /**
     * This main method only executes under JDK 1.6. It is used to generate the
     * MAPPER entries to standard out. They can then be copied and pasted back
     * into this class. Practically speaking, we will never need to execute this
     * main method again as the mappings are formed according to Unicode
     * standards that will not changed, but this code serves as documentation
     * to trace how the values were produced.
     */
/*
    public static void main(String[] args) {
        // loop through all latin characters to the end of "Latin Extended-B"
        for (int i = 0; i <= 591; i++) {
            // get the latin character to consider
            final char c = (char) i;

            // decompose the character into its parts
            final String decomposed = Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD);

            // use the first character in the decomposition String as the normalized character
            final char normalized = decomposed.charAt(0);

            // determine whether c is a special character, and what its value should be in the mapper
            boolean specialChar = true;
            String mapperValue;

            if (normalized == '\n') {
                mapperValue = "\\n";
            } else if (normalized == '\t') {
                mapperValue = "\\t";
            } else if (normalized == '\r') {
                mapperValue = "\\r";
            } else if (normalized == '\'') {
                mapperValue = "\\'";
            } else if (normalized == '\\') {
                mapperValue = "\\\\";
            } else {
                specialChar = Character.isISOControl(c);
                mapperValue = Integer.toHexString(normalized);
                while (mapperValue.length() < 4)
                    mapperValue = "0" + mapperValue;

                mapperValue = "\\u" + mapperValue;
            }

            // create a comment that follows the mapper entry, if appropriate
            final String comment = specialChar ? "" :  (" // (" + c + " -> " + normalized + ")");

            System.out.println("MAPPER[" + i + "] = '" + mapperValue + "';" + comment);
        }
    }
*/

    static {
        MAPPER[0] = '\u0000';
        MAPPER[1] = '\u0001';
        MAPPER[2] = '\u0002';
        MAPPER[3] = '\u0003';
        MAPPER[4] = '\u0004';
        MAPPER[5] = '\u0005';
        MAPPER[6] = '\u0006';
        MAPPER[7] = '\u0007';
        MAPPER[8] = '\u0008';
        MAPPER[9] = '\t';
        MAPPER[10] = '\n';
        MAPPER[11] = '\u000b';
        MAPPER[12] = '\u000c';
        MAPPER[13] = '\r';
        MAPPER[14] = '\u000e';
        MAPPER[15] = '\u000f';
        MAPPER[16] = '\u0010';
        MAPPER[17] = '\u0011';
        MAPPER[18] = '\u0012';
        MAPPER[19] = '\u0013';
        MAPPER[20] = '\u0014';
        MAPPER[21] = '\u0015';
        MAPPER[22] = '\u0016';
        MAPPER[23] = '\u0017';
        MAPPER[24] = '\u0018';
        MAPPER[25] = '\u0019';
        MAPPER[26] = '\u001a';
        MAPPER[27] = '\u001b';
        MAPPER[28] = '\u001c';
        MAPPER[29] = '\u001d';
        MAPPER[30] = '\u001e';
        MAPPER[31] = '\u001f';
        MAPPER[32] = '\u0020'; // (  ->  )
        MAPPER[33] = '\u0021'; // (! -> !)
        MAPPER[34] = '\u0022'; // (" -> ")
        MAPPER[35] = '\u0023'; // (# -> #)
        MAPPER[36] = '\u0024'; // ($ -> $)
        MAPPER[37] = '\u0025'; // (% -> %)
        MAPPER[38] = '\u0026'; // (& -> &)
        MAPPER[39] = '\'';
        MAPPER[40] = '\u0028'; // (( -> ()
        MAPPER[41] = '\u0029'; // () -> ))
        MAPPER[42] = '\u002a'; // (* -> *)
        MAPPER[43] = '\u002b'; // (+ -> +)
        MAPPER[44] = '\u002c'; // (, -> ,)
        MAPPER[45] = '\u002d'; // (- -> -)
        MAPPER[46] = '\u002e'; // (. -> .)
        MAPPER[47] = '\u002f'; // (/ -> /)
        MAPPER[48] = '\u0030'; // (0 -> 0)
        MAPPER[49] = '\u0031'; // (1 -> 1)
        MAPPER[50] = '\u0032'; // (2 -> 2)
        MAPPER[51] = '\u0033'; // (3 -> 3)
        MAPPER[52] = '\u0034'; // (4 -> 4)
        MAPPER[53] = '\u0035'; // (5 -> 5)
        MAPPER[54] = '\u0036'; // (6 -> 6)
        MAPPER[55] = '\u0037'; // (7 -> 7)
        MAPPER[56] = '\u0038'; // (8 -> 8)
        MAPPER[57] = '\u0039'; // (9 -> 9)
        MAPPER[58] = '\u003a'; // (: -> :)
        MAPPER[59] = '\u003b'; // (; -> ;)
        MAPPER[60] = '\u003c'; // (< -> <)
        MAPPER[61] = '\u003d'; // (= -> =)
        MAPPER[62] = '\u003e'; // (> -> >)
        MAPPER[63] = '\u003f'; // (? -> ?)
        MAPPER[64] = '\u0040'; // (@ -> @)
        MAPPER[65] = '\u0041'; // (A -> A)
        MAPPER[66] = '\u0042'; // (B -> B)
        MAPPER[67] = '\u0043'; // (C -> C)
        MAPPER[68] = '\u0044'; // (D -> D)
        MAPPER[69] = '\u0045'; // (E -> E)
        MAPPER[70] = '\u0046'; // (F -> F)
        MAPPER[71] = '\u0047'; // (G -> G)
        MAPPER[72] = '\u0048'; // (H -> H)
        MAPPER[73] = '\u0049'; // (I -> I)
        MAPPER[74] = '\u004a'; // (J -> J)
        MAPPER[75] = '\u004b'; // (K -> K)
        MAPPER[76] = '\u004c'; // (L -> L)
        MAPPER[77] = '\u004d'; // (M -> M)
        MAPPER[78] = '\u004e'; // (N -> N)
        MAPPER[79] = '\u004f'; // (O -> O)
        MAPPER[80] = '\u0050'; // (P -> P)
        MAPPER[81] = '\u0051'; // (Q -> Q)
        MAPPER[82] = '\u0052'; // (R -> R)
        MAPPER[83] = '\u0053'; // (S -> S)
        MAPPER[84] = '\u0054'; // (T -> T)
        MAPPER[85] = '\u0055'; // (U -> U)
        MAPPER[86] = '\u0056'; // (V -> V)
        MAPPER[87] = '\u0057'; // (W -> W)
        MAPPER[88] = '\u0058'; // (X -> X)
        MAPPER[89] = '\u0059'; // (Y -> Y)
        MAPPER[90] = '\u005a'; // (Z -> Z)
        MAPPER[91] = '\u005b'; // ([ -> [)
        MAPPER[92] = '\\';
        MAPPER[93] = '\u005d'; // (] -> ])
        MAPPER[94] = '\u005e'; // (^ -> ^)
        MAPPER[95] = '\u005f'; // (_ -> _)
        MAPPER[96] = '\u0060'; // (` -> `)
        MAPPER[97] = '\u0061'; // (a -> a)
        MAPPER[98] = '\u0062'; // (b -> b)
        MAPPER[99] = '\u0063'; // (c -> c)
        MAPPER[100] = '\u0064'; // (d -> d)
        MAPPER[101] = '\u0065'; // (e -> e)
        MAPPER[102] = '\u0066'; // (f -> f)
        MAPPER[103] = '\u0067'; // (g -> g)
        MAPPER[104] = '\u0068'; // (h -> h)
        MAPPER[105] = '\u0069'; // (i -> i)
        MAPPER[106] = '\u006a'; // (j -> j)
        MAPPER[107] = '\u006b'; // (k -> k)
        MAPPER[108] = '\u006c'; // (l -> l)
        MAPPER[109] = '\u006d'; // (m -> m)
        MAPPER[110] = '\u006e'; // (n -> n)
        MAPPER[111] = '\u006f'; // (o -> o)
        MAPPER[112] = '\u0070'; // (p -> p)
        MAPPER[113] = '\u0071'; // (q -> q)
        MAPPER[114] = '\u0072'; // (r -> r)
        MAPPER[115] = '\u0073'; // (s -> s)
        MAPPER[116] = '\u0074'; // (t -> t)
        MAPPER[117] = '\u0075'; // (u -> u)
        MAPPER[118] = '\u0076'; // (v -> v)
        MAPPER[119] = '\u0077'; // (w -> w)
        MAPPER[120] = '\u0078'; // (x -> x)
        MAPPER[121] = '\u0079'; // (y -> y)
        MAPPER[122] = '\u007a'; // (z -> z)
        MAPPER[123] = '\u007b'; // ({ -> {)
        MAPPER[124] = '\u007c'; // (| -> |)
        MAPPER[125] = '\u007d'; // (} -> })
        MAPPER[126] = '\u007e'; // (~ -> ~)
        MAPPER[127] = '\u007f';
        MAPPER[128] = '\u0080';
        MAPPER[129] = '\u0081';
        MAPPER[130] = '\u0082';
        MAPPER[131] = '\u0083';
        MAPPER[132] = '\u0084';
        MAPPER[133] = '\u0085';
        MAPPER[134] = '\u0086';
        MAPPER[135] = '\u0087';
        MAPPER[136] = '\u0088';
        MAPPER[137] = '\u0089';
        MAPPER[138] = '\u008a';
        MAPPER[139] = '\u008b';
        MAPPER[140] = '\u008c';
        MAPPER[141] = '\u008d';
        MAPPER[142] = '\u008e';
        MAPPER[143] = '\u008f';
        MAPPER[144] = '\u0090';
        MAPPER[145] = '\u0091';
        MAPPER[146] = '\u0092';
        MAPPER[147] = '\u0093';
        MAPPER[148] = '\u0094';
        MAPPER[149] = '\u0095';
        MAPPER[150] = '\u0096';
        MAPPER[151] = '\u0097';
        MAPPER[152] = '\u0098';
        MAPPER[153] = '\u0099';
        MAPPER[154] = '\u009a';
        MAPPER[155] = '\u009b';
        MAPPER[156] = '\u009c';
        MAPPER[157] = '\u009d';
        MAPPER[158] = '\u009e';
        MAPPER[159] = '\u009f';
        MAPPER[160] = '\u00a0'; // (  ->  )
        MAPPER[161] = '\u00a1'; // (¡ -> ¡)
        MAPPER[162] = '\u00a2'; // (¢ -> ¢)
        MAPPER[163] = '\u00a3'; // (£ -> £)
        MAPPER[164] = '\u00a4'; // (¤ -> ¤)
        MAPPER[165] = '\u00a5'; // (¥ -> ¥)
        MAPPER[166] = '\u00a6'; // (¦ -> ¦)
        MAPPER[167] = '\u00a7'; // (§ -> §)
        MAPPER[168] = '\u00a8'; // (¨ -> ¨)
        MAPPER[169] = '\u00a9'; // (© -> ©)
        MAPPER[170] = '\u00aa'; // (ª -> ª)
        MAPPER[171] = '\u00ab'; // (« -> «)
        MAPPER[172] = '\u00ac'; // (¬ -> ¬)
        MAPPER[173] = '\u00ad'; // (­ -> ­)
        MAPPER[174] = '\u00ae'; // (® -> ®)
        MAPPER[175] = '\u00af'; // (¯ -> ¯)
        MAPPER[176] = '\u00b0'; // (° -> °)
        MAPPER[177] = '\u00b1'; // (± -> ±)
        MAPPER[178] = '\u00b2'; // (² -> ²)
        MAPPER[179] = '\u00b3'; // (³ -> ³)
        MAPPER[180] = '\u00b4'; // (´ -> ´)
        MAPPER[181] = '\u00b5'; // (µ -> µ)
        MAPPER[182] = '\u00b6'; // (¶ -> ¶)
        MAPPER[183] = '\u00b7'; // (· -> ·)
        MAPPER[184] = '\u00b8'; // (¸ -> ¸)
        MAPPER[185] = '\u00b9'; // (¹ -> ¹)
        MAPPER[186] = '\u00ba'; // (º -> º)
        MAPPER[187] = '\u00bb'; // (» -> »)
        MAPPER[188] = '\u00bc'; // (¼ -> ¼)
        MAPPER[189] = '\u00bd'; // (½ -> ½)
        MAPPER[190] = '\u00be'; // (¾ -> ¾)
        MAPPER[191] = '\u00bf'; // (¿ -> ¿)
        MAPPER[192] = '\u0041'; // (À -> A)
        MAPPER[193] = '\u0041'; // (Á -> A)
        MAPPER[194] = '\u0041'; // (Â -> A)
        MAPPER[195] = '\u0041'; // (Ã -> A)
        MAPPER[196] = '\u0041'; // (Ä -> A)
        MAPPER[197] = '\u0041'; // (Å -> A)
        MAPPER[198] = '\u00c6'; // (Æ -> Æ)
        MAPPER[199] = '\u0043'; // (Ç -> C)
        MAPPER[200] = '\u0045'; // (È -> E)
        MAPPER[201] = '\u0045'; // (É -> E)
        MAPPER[202] = '\u0045'; // (Ê -> E)
        MAPPER[203] = '\u0045'; // (Ë -> E)
        MAPPER[204] = '\u0049'; // (Ì -> I)
        MAPPER[205] = '\u0049'; // (Í -> I)
        MAPPER[206] = '\u0049'; // (Î -> I)
        MAPPER[207] = '\u0049'; // (Ï -> I)
        MAPPER[208] = '\u00d0'; // (Ð -> Ð)
        MAPPER[209] = '\u004e'; // (Ñ -> N)
        MAPPER[210] = '\u004f'; // (Ò -> O)
        MAPPER[211] = '\u004f'; // (Ó -> O)
        MAPPER[212] = '\u004f'; // (Ô -> O)
        MAPPER[213] = '\u004f'; // (Õ -> O)
        MAPPER[214] = '\u004f'; // (Ö -> O)
        MAPPER[215] = '\u00d7'; // (× -> ×)
        MAPPER[216] = '\u00d8'; // (Ø -> Ø)
        MAPPER[217] = '\u0055'; // (Ù -> U)
        MAPPER[218] = '\u0055'; // (Ú -> U)
        MAPPER[219] = '\u0055'; // (Û -> U)
        MAPPER[220] = '\u0055'; // (Ü -> U)
        MAPPER[221] = '\u0059'; // (Ý -> Y)
        MAPPER[222] = '\u00de'; // (Þ -> Þ)
        MAPPER[223] = '\u00df'; // (ß -> ß)
        MAPPER[224] = '\u0061'; // (à -> a)
        MAPPER[225] = '\u0061'; // (á -> a)
        MAPPER[226] = '\u0061'; // (â -> a)
        MAPPER[227] = '\u0061'; // (ã -> a)
        MAPPER[228] = '\u0061'; // (ä -> a)
        MAPPER[229] = '\u0061'; // (å -> a)
        MAPPER[230] = '\u00e6'; // (æ -> æ)
        MAPPER[231] = '\u0063'; // (ç -> c)
        MAPPER[232] = '\u0065'; // (è -> e)
        MAPPER[233] = '\u0065'; // (é -> e)
        MAPPER[234] = '\u0065'; // (ê -> e)
        MAPPER[235] = '\u0065'; // (ë -> e)
        MAPPER[236] = '\u0069'; // (ì -> i)
        MAPPER[237] = '\u0069'; // (í -> i)
        MAPPER[238] = '\u0069'; // (î -> i)
        MAPPER[239] = '\u0069'; // (ï -> i)
        MAPPER[240] = '\u00f0'; // (ð -> ð)
        MAPPER[241] = '\u006e'; // (ñ -> n)
        MAPPER[242] = '\u006f'; // (ò -> o)
        MAPPER[243] = '\u006f'; // (ó -> o)
        MAPPER[244] = '\u006f'; // (ô -> o)
        MAPPER[245] = '\u006f'; // (õ -> o)
        MAPPER[246] = '\u006f'; // (ö -> o)
        MAPPER[247] = '\u00f7'; // (÷ -> ÷)
        MAPPER[248] = '\u00f8'; // (ø -> ø)
        MAPPER[249] = '\u0075'; // (ù -> u)
        MAPPER[250] = '\u0075'; // (ú -> u)
        MAPPER[251] = '\u0075'; // (û -> u)
        MAPPER[252] = '\u0075'; // (ü -> u)
        MAPPER[253] = '\u0079'; // (ý -> y)
        MAPPER[254] = '\u00fe'; // (þ -> þ)
        MAPPER[255] = '\u0079'; // (ÿ -> y)
        MAPPER[256] = '\u0041'; // (? -> A)
        MAPPER[257] = '\u0061'; // (? -> a)
        MAPPER[258] = '\u0041'; // (? -> A)
        MAPPER[259] = '\u0061'; // (? -> a)
        MAPPER[260] = '\u0041'; // (? -> A)
        MAPPER[261] = '\u0061'; // (? -> a)
        MAPPER[262] = '\u0043'; // (? -> C)
        MAPPER[263] = '\u0063'; // (? -> c)
        MAPPER[264] = '\u0043'; // (? -> C)
        MAPPER[265] = '\u0063'; // (? -> c)
        MAPPER[266] = '\u0043'; // (? -> C)
        MAPPER[267] = '\u0063'; // (? -> c)
        MAPPER[268] = '\u0043'; // (? -> C)
        MAPPER[269] = '\u0063'; // (? -> c)
        MAPPER[270] = '\u0044'; // (? -> D)
        MAPPER[271] = '\u0064'; // (? -> d)
        MAPPER[272] = '\u0110'; // (? -> ?)
        MAPPER[273] = '\u0111'; // (? -> ?)
        MAPPER[274] = '\u0045'; // (? -> E)
        MAPPER[275] = '\u0065'; // (? -> e)
        MAPPER[276] = '\u0045'; // (? -> E)
        MAPPER[277] = '\u0065'; // (? -> e)
        MAPPER[278] = '\u0045'; // (? -> E)
        MAPPER[279] = '\u0065'; // (? -> e)
        MAPPER[280] = '\u0045'; // (? -> E)
        MAPPER[281] = '\u0065'; // (? -> e)
        MAPPER[282] = '\u0045'; // (? -> E)
        MAPPER[283] = '\u0065'; // (? -> e)
        MAPPER[284] = '\u0047'; // (? -> G)
        MAPPER[285] = '\u0067'; // (? -> g)
        MAPPER[286] = '\u0047'; // (? -> G)
        MAPPER[287] = '\u0067'; // (? -> g)
        MAPPER[288] = '\u0047'; // (? -> G)
        MAPPER[289] = '\u0067'; // (? -> g)
        MAPPER[290] = '\u0047'; // (? -> G)
        MAPPER[291] = '\u0067'; // (? -> g)
        MAPPER[292] = '\u0048'; // (? -> H)
        MAPPER[293] = '\u0068'; // (? -> h)
        MAPPER[294] = '\u0126'; // (? -> ?)
        MAPPER[295] = '\u0127'; // (? -> ?)
        MAPPER[296] = '\u0049'; // (? -> I)
        MAPPER[297] = '\u0069'; // (? -> i)
        MAPPER[298] = '\u0049'; // (? -> I)
        MAPPER[299] = '\u0069'; // (? -> i)
        MAPPER[300] = '\u0049'; // (? -> I)
        MAPPER[301] = '\u0069'; // (? -> i)
        MAPPER[302] = '\u0049'; // (? -> I)
        MAPPER[303] = '\u0069'; // (? -> i)
        MAPPER[304] = '\u0049'; // (? -> I)
        MAPPER[305] = '\u0131'; // (? -> ?)
        MAPPER[306] = '\u0132'; // (? -> ?)
        MAPPER[307] = '\u0133'; // (? -> ?)
        MAPPER[308] = '\u004a'; // (? -> J)
        MAPPER[309] = '\u006a'; // (? -> j)
        MAPPER[310] = '\u004b'; // (? -> K)
        MAPPER[311] = '\u006b'; // (? -> k)
        MAPPER[312] = '\u0138'; // (? -> ?)
        MAPPER[313] = '\u004c'; // (? -> L)
        MAPPER[314] = '\u006c'; // (? -> l)
        MAPPER[315] = '\u004c'; // (? -> L)
        MAPPER[316] = '\u006c'; // (? -> l)
        MAPPER[317] = '\u004c'; // (? -> L)
        MAPPER[318] = '\u006c'; // (? -> l)
        MAPPER[319] = '\u013f'; // (? -> ?)
        MAPPER[320] = '\u0140'; // (? -> ?)
        MAPPER[321] = '\u0141'; // (? -> ?)
        MAPPER[322] = '\u0142'; // (? -> ?)
        MAPPER[323] = '\u004e'; // (? -> N)
        MAPPER[324] = '\u006e'; // (? -> n)
        MAPPER[325] = '\u004e'; // (? -> N)
        MAPPER[326] = '\u006e'; // (? -> n)
        MAPPER[327] = '\u004e'; // (? -> N)
        MAPPER[328] = '\u006e'; // (? -> n)
        MAPPER[329] = '\u0149'; // (? -> ?)
        MAPPER[330] = '\u014a'; // (? -> ?)
        MAPPER[331] = '\u014b'; // (? -> ?)
        MAPPER[332] = '\u004f'; // (? -> O)
        MAPPER[333] = '\u006f'; // (? -> o)
        MAPPER[334] = '\u004f'; // (? -> O)
        MAPPER[335] = '\u006f'; // (? -> o)
        MAPPER[336] = '\u004f'; // (? -> O)
        MAPPER[337] = '\u006f'; // (? -> o)
        MAPPER[338] = '\u0152'; // (Œ -> Œ)
        MAPPER[339] = '\u0153'; // (œ -> œ)
        MAPPER[340] = '\u0052'; // (? -> R)
        MAPPER[341] = '\u0072'; // (? -> r)
        MAPPER[342] = '\u0052'; // (? -> R)
        MAPPER[343] = '\u0072'; // (? -> r)
        MAPPER[344] = '\u0052'; // (? -> R)
        MAPPER[345] = '\u0072'; // (? -> r)
        MAPPER[346] = '\u0053'; // (? -> S)
        MAPPER[347] = '\u0073'; // (? -> s)
        MAPPER[348] = '\u0053'; // (? -> S)
        MAPPER[349] = '\u0073'; // (? -> s)
        MAPPER[350] = '\u0053'; // (? -> S)
        MAPPER[351] = '\u0073'; // (? -> s)
        MAPPER[352] = '\u0053'; // (Š -> S)
        MAPPER[353] = '\u0073'; // (š -> s)
        MAPPER[354] = '\u0054'; // (? -> T)
        MAPPER[355] = '\u0074'; // (? -> t)
        MAPPER[356] = '\u0054'; // (? -> T)
        MAPPER[357] = '\u0074'; // (? -> t)
        MAPPER[358] = '\u0166'; // (? -> ?)
        MAPPER[359] = '\u0167'; // (? -> ?)
        MAPPER[360] = '\u0055'; // (? -> U)
        MAPPER[361] = '\u0075'; // (? -> u)
        MAPPER[362] = '\u0055'; // (? -> U)
        MAPPER[363] = '\u0075'; // (? -> u)
        MAPPER[364] = '\u0055'; // (? -> U)
        MAPPER[365] = '\u0075'; // (? -> u)
        MAPPER[366] = '\u0055'; // (? -> U)
        MAPPER[367] = '\u0075'; // (? -> u)
        MAPPER[368] = '\u0055'; // (? -> U)
        MAPPER[369] = '\u0075'; // (? -> u)
        MAPPER[370] = '\u0055'; // (? -> U)
        MAPPER[371] = '\u0075'; // (? -> u)
        MAPPER[372] = '\u0057'; // (? -> W)
        MAPPER[373] = '\u0077'; // (? -> w)
        MAPPER[374] = '\u0059'; // (? -> Y)
        MAPPER[375] = '\u0079'; // (? -> y)
        MAPPER[376] = '\u0059'; // (Ÿ -> Y)
        MAPPER[377] = '\u005a'; // (? -> Z)
        MAPPER[378] = '\u007a'; // (? -> z)
        MAPPER[379] = '\u005a'; // (? -> Z)
        MAPPER[380] = '\u007a'; // (? -> z)
        MAPPER[381] = '\u005a'; // (Ž -> Z)
        MAPPER[382] = '\u007a'; // (ž -> z)
        MAPPER[383] = '\u017f'; // (? -> ?)
        MAPPER[384] = '\u0180'; // (? -> ?)
        MAPPER[385] = '\u0181'; // (? -> ?)
        MAPPER[386] = '\u0182'; // (? -> ?)
        MAPPER[387] = '\u0183'; // (? -> ?)
        MAPPER[388] = '\u0184'; // (? -> ?)
        MAPPER[389] = '\u0185'; // (? -> ?)
        MAPPER[390] = '\u0186'; // (? -> ?)
        MAPPER[391] = '\u0187'; // (? -> ?)
        MAPPER[392] = '\u0188'; // (? -> ?)
        MAPPER[393] = '\u0189'; // (? -> ?)
        MAPPER[394] = '\u018a'; // (? -> ?)
        MAPPER[395] = '\u018b'; // (? -> ?)
        MAPPER[396] = '\u018c'; // (? -> ?)
        MAPPER[397] = '\u018d'; // (? -> ?)
        MAPPER[398] = '\u018e'; // (? -> ?)
        MAPPER[399] = '\u018f'; // (? -> ?)
        MAPPER[400] = '\u0190'; // (? -> ?)
        MAPPER[401] = '\u0191'; // (? -> ?)
        MAPPER[402] = '\u0192'; // (ƒ -> ƒ)
        MAPPER[403] = '\u0193'; // (? -> ?)
        MAPPER[404] = '\u0194'; // (? -> ?)
        MAPPER[405] = '\u0195'; // (? -> ?)
        MAPPER[406] = '\u0196'; // (? -> ?)
        MAPPER[407] = '\u0197'; // (? -> ?)
        MAPPER[408] = '\u0198'; // (? -> ?)
        MAPPER[409] = '\u0199'; // (? -> ?)
        MAPPER[410] = '\u019a'; // (? -> ?)
        MAPPER[411] = '\u019b'; // (? -> ?)
        MAPPER[412] = '\u019c'; // (? -> ?)
        MAPPER[413] = '\u019d'; // (? -> ?)
        MAPPER[414] = '\u019e'; // (? -> ?)
        MAPPER[415] = '\u019f'; // (? -> ?)
        MAPPER[416] = '\u004f'; // (? -> O)
        MAPPER[417] = '\u006f'; // (? -> o)
        MAPPER[418] = '\u01a2'; // (? -> ?)
        MAPPER[419] = '\u01a3'; // (? -> ?)
        MAPPER[420] = '\u01a4'; // (? -> ?)
        MAPPER[421] = '\u01a5'; // (? -> ?)
        MAPPER[422] = '\u01a6'; // (? -> ?)
        MAPPER[423] = '\u01a7'; // (? -> ?)
        MAPPER[424] = '\u01a8'; // (? -> ?)
        MAPPER[425] = '\u01a9'; // (? -> ?)
        MAPPER[426] = '\u01aa'; // (? -> ?)
        MAPPER[427] = '\u01ab'; // (? -> ?)
        MAPPER[428] = '\u01ac'; // (? -> ?)
        MAPPER[429] = '\u01ad'; // (? -> ?)
        MAPPER[430] = '\u01ae'; // (? -> ?)
        MAPPER[431] = '\u0055'; // (? -> U)
        MAPPER[432] = '\u0075'; // (? -> u)
        MAPPER[433] = '\u01b1'; // (? -> ?)
        MAPPER[434] = '\u01b2'; // (? -> ?)
        MAPPER[435] = '\u01b3'; // (? -> ?)
        MAPPER[436] = '\u01b4'; // (? -> ?)
        MAPPER[437] = '\u01b5'; // (? -> ?)
        MAPPER[438] = '\u01b6'; // (? -> ?)
        MAPPER[439] = '\u01b7'; // (? -> ?)
        MAPPER[440] = '\u01b8'; // (? -> ?)
        MAPPER[441] = '\u01b9'; // (? -> ?)
        MAPPER[442] = '\u01ba'; // (? -> ?)
        MAPPER[443] = '\u01bb'; // (? -> ?)
        MAPPER[444] = '\u01bc'; // (? -> ?)
        MAPPER[445] = '\u01bd'; // (? -> ?)
        MAPPER[446] = '\u01be'; // (? -> ?)
        MAPPER[447] = '\u01bf'; // (? -> ?)
        MAPPER[448] = '\u01c0'; // (? -> ?)
        MAPPER[449] = '\u01c1'; // (? -> ?)
        MAPPER[450] = '\u01c2'; // (? -> ?)
        MAPPER[451] = '\u01c3'; // (? -> ?)
        MAPPER[452] = '\u01c4'; // (? -> ?)
        MAPPER[453] = '\u01c5'; // (? -> ?)
        MAPPER[454] = '\u01c6'; // (? -> ?)
        MAPPER[455] = '\u01c7'; // (? -> ?)
        MAPPER[456] = '\u01c8'; // (? -> ?)
        MAPPER[457] = '\u01c9'; // (? -> ?)
        MAPPER[458] = '\u01ca'; // (? -> ?)
        MAPPER[459] = '\u01cb'; // (? -> ?)
        MAPPER[460] = '\u01cc'; // (? -> ?)
        MAPPER[461] = '\u0041'; // (? -> A)
        MAPPER[462] = '\u0061'; // (? -> a)
        MAPPER[463] = '\u0049'; // (? -> I)
        MAPPER[464] = '\u0069'; // (? -> i)
        MAPPER[465] = '\u004f'; // (? -> O)
        MAPPER[466] = '\u006f'; // (? -> o)
        MAPPER[467] = '\u0055'; // (? -> U)
        MAPPER[468] = '\u0075'; // (? -> u)
        MAPPER[469] = '\u0055'; // (? -> U)
        MAPPER[470] = '\u0075'; // (? -> u)
        MAPPER[471] = '\u0055'; // (? -> U)
        MAPPER[472] = '\u0075'; // (? -> u)
        MAPPER[473] = '\u0055'; // (? -> U)
        MAPPER[474] = '\u0075'; // (? -> u)
        MAPPER[475] = '\u0055'; // (? -> U)
        MAPPER[476] = '\u0075'; // (? -> u)
        MAPPER[477] = '\u01dd'; // (? -> ?)
        MAPPER[478] = '\u0041'; // (? -> A)
        MAPPER[479] = '\u0061'; // (? -> a)
        MAPPER[480] = '\u0041'; // (? -> A)
        MAPPER[481] = '\u0061'; // (? -> a)
        MAPPER[482] = '\u00c6'; // (? -> Æ)
        MAPPER[483] = '\u00e6'; // (? -> æ)
        MAPPER[484] = '\u01e4'; // (? -> ?)
        MAPPER[485] = '\u01e5'; // (? -> ?)
        MAPPER[486] = '\u0047'; // (? -> G)
        MAPPER[487] = '\u0067'; // (? -> g)
        MAPPER[488] = '\u004b'; // (? -> K)
        MAPPER[489] = '\u006b'; // (? -> k)
        MAPPER[490] = '\u004f'; // (? -> O)
        MAPPER[491] = '\u006f'; // (? -> o)
        MAPPER[492] = '\u004f'; // (? -> O)
        MAPPER[493] = '\u006f'; // (? -> o)
        MAPPER[494] = '\u01b7'; // (? -> ?)
        MAPPER[495] = '\u0292'; // (? -> ?)
        MAPPER[496] = '\u006a'; // (? -> j)
        MAPPER[497] = '\u01f1'; // (? -> ?)
        MAPPER[498] = '\u01f2'; // (? -> ?)
        MAPPER[499] = '\u01f3'; // (? -> ?)
        MAPPER[500] = '\u0047'; // (? -> G)
        MAPPER[501] = '\u0067'; // (? -> g)
        MAPPER[502] = '\u01f6'; // (? -> ?)
        MAPPER[503] = '\u01f7'; // (? -> ?)
        MAPPER[504] = '\u004e'; // (? -> N)
        MAPPER[505] = '\u006e'; // (? -> n)
        MAPPER[506] = '\u0041'; // (? -> A)
        MAPPER[507] = '\u0061'; // (? -> a)
        MAPPER[508] = '\u00c6'; // (? -> Æ)
        MAPPER[509] = '\u00e6'; // (? -> æ)
        MAPPER[510] = '\u00d8'; // (? -> Ø)
        MAPPER[511] = '\u00f8'; // (? -> ø)
        MAPPER[512] = '\u0041'; // (? -> A)
        MAPPER[513] = '\u0061'; // (? -> a)
        MAPPER[514] = '\u0041'; // (? -> A)
        MAPPER[515] = '\u0061'; // (? -> a)
        MAPPER[516] = '\u0045'; // (? -> E)
        MAPPER[517] = '\u0065'; // (? -> e)
        MAPPER[518] = '\u0045'; // (? -> E)
        MAPPER[519] = '\u0065'; // (? -> e)
        MAPPER[520] = '\u0049'; // (? -> I)
        MAPPER[521] = '\u0069'; // (? -> i)
        MAPPER[522] = '\u0049'; // (? -> I)
        MAPPER[523] = '\u0069'; // (? -> i)
        MAPPER[524] = '\u004f'; // (? -> O)
        MAPPER[525] = '\u006f'; // (? -> o)
        MAPPER[526] = '\u004f'; // (? -> O)
        MAPPER[527] = '\u006f'; // (? -> o)
        MAPPER[528] = '\u0052'; // (? -> R)
        MAPPER[529] = '\u0072'; // (? -> r)
        MAPPER[530] = '\u0052'; // (? -> R)
        MAPPER[531] = '\u0072'; // (? -> r)
        MAPPER[532] = '\u0055'; // (? -> U)
        MAPPER[533] = '\u0075'; // (? -> u)
        MAPPER[534] = '\u0055'; // (? -> U)
        MAPPER[535] = '\u0075'; // (? -> u)
        MAPPER[536] = '\u0053'; // (? -> S)
        MAPPER[537] = '\u0073'; // (? -> s)
        MAPPER[538] = '\u0054'; // (? -> T)
        MAPPER[539] = '\u0074'; // (? -> t)
        MAPPER[540] = '\u021c'; // (? -> ?)
        MAPPER[541] = '\u021d'; // (? -> ?)
        MAPPER[542] = '\u0048'; // (? -> H)
        MAPPER[543] = '\u0068'; // (? -> h)
        MAPPER[544] = '\u0220'; // (? -> ?)
        MAPPER[545] = '\u0221'; // (? -> ?)
        MAPPER[546] = '\u0222'; // (? -> ?)
        MAPPER[547] = '\u0223'; // (? -> ?)
        MAPPER[548] = '\u0224'; // (? -> ?)
        MAPPER[549] = '\u0225'; // (? -> ?)
        MAPPER[550] = '\u0041'; // (? -> A)
        MAPPER[551] = '\u0061'; // (? -> a)
        MAPPER[552] = '\u0045'; // (? -> E)
        MAPPER[553] = '\u0065'; // (? -> e)
        MAPPER[554] = '\u004f'; // (? -> O)
        MAPPER[555] = '\u006f'; // (? -> o)
        MAPPER[556] = '\u004f'; // (? -> O)
        MAPPER[557] = '\u006f'; // (? -> o)
        MAPPER[558] = '\u004f'; // (? -> O)
        MAPPER[559] = '\u006f'; // (? -> o)
        MAPPER[560] = '\u004f'; // (? -> O)
        MAPPER[561] = '\u006f'; // (? -> o)
        MAPPER[562] = '\u0059'; // (? -> Y)
        MAPPER[563] = '\u0079'; // (? -> y)
        MAPPER[564] = '\u0234'; // (? -> ?)
        MAPPER[565] = '\u0235'; // (? -> ?)
        MAPPER[566] = '\u0236'; // (? -> ?)
        MAPPER[567] = '\u0237'; // (? -> ?)
        MAPPER[568] = '\u0238'; // (? -> ?)
        MAPPER[569] = '\u0239'; // (? -> ?)
        MAPPER[570] = '\u023a'; // (? -> ?)
        MAPPER[571] = '\u023b'; // (? -> ?)
        MAPPER[572] = '\u023c'; // (? -> ?)
        MAPPER[573] = '\u023d'; // (? -> ?)
        MAPPER[574] = '\u023e'; // (? -> ?)
        MAPPER[575] = '\u023f'; // (? -> ?)
        MAPPER[576] = '\u0240'; // (? -> ?)
        MAPPER[577] = '\u0241'; // (? -> ?)
        MAPPER[578] = '\u0242'; // (? -> ?)
        MAPPER[579] = '\u0243'; // (? -> ?)
        MAPPER[580] = '\u0244'; // (? -> ?)
        MAPPER[581] = '\u0245'; // (? -> ?)
        MAPPER[582] = '\u0246'; // (? -> ?)
        MAPPER[583] = '\u0247'; // (? -> ?)
        MAPPER[584] = '\u0248'; // (? -> ?)
        MAPPER[585] = '\u0249'; // (? -> ?)
        MAPPER[586] = '\u024a'; // (? -> ?)
        MAPPER[587] = '\u024b'; // (? -> ?)
        MAPPER[588] = '\u024c'; // (? -> ?)
        MAPPER[589] = '\u024d'; // (? -> ?)
        MAPPER[590] = '\u024e'; // (? -> ?)
        MAPPER[591] = '\u024f'; // (? -> ?)
    }
}