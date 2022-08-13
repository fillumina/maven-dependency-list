#!/bin/bash                    
# see: https://gist.github.com/briandealwis/782862/9cc9ef8a78af3bb78a692313f8bfa6fb76ab4663
                                                                                                                     
# simple script for turning a jar with a Main-Class                                                                                                 
# into a stand alone executable                                                                                                                     
# cat [your jar file] >> [this file]                                                                                                                
# then chmod +x [this file]                                                                                                                         
# you can now exec [this file]                                                                                                                      

commandToRun="$(printf "%q " "$@")"
if test "$commandToRun" = "'' "; then
  eval "java -Xmx1G -jar $0"
else
  eval "java -Xmx1G -jar $0 $commandToRun"
fi
exit
PK
    ��U            	   META-INF/PK   ��U���̆   �      META-INF/MANIFEST.MF5��
�@@���;�oi'A����C�(�+��Y���{�%����<'��#��g�����ZZY��tp��)
',�����k���'��x��%QWGJ�?�K�y%>�y�?��'���7�����5�Yc�PK
    ��U               com/PK
    ��U               com/fillumina/PK
    ��U               com/fillumina/maven/PK
    ��U               com/fillumina/maven/reverse/PK
    ��U            '   com/fillumina/maven/reverse/dependency/PK
    ��U               META-INF/maven/PK
    ��U            6   META-INF/maven/com.fillumina.maven.reverse.dependency/PK
    ��U            O   META-INF/maven/com.fillumina.maven.reverse.dependency/maven-reverse-dependency/PK   ��UUՠ��  2  6   com/fillumina/maven/reverse/dependency/PomLoader.class�W�s���%yey��`��iB �dy	i ����(�2��)}��ky��UW+��w�&}��G�J��+LgL�̴��ә�G��M�{�2X3�t<�{��9���{���� ��w1 d l"�A�����.;Ւ��z]���n�fB�ɕ�231S ��7�Jٮ9~]�u��WhTd5�m����B����Uy3[�N� �⓹~u3�
Ltů,��{�TP�bT��z�Ҷ�E�)2�S�^�N��]����d��VK*�1�q@ V��|S���s] �������O�x ��=�
�'�L��!��0>"p��jD��c-�\Ϟw�23����}�]�*��0�a��8�G�b%���ʲ��@�n��4y����TD��CF`�����"cHb�@��4l������r��V��ܐUۗ7����&�EY-lڗ�J�st�{�F���|�I�l��c��~m;�����3ʹ�M��#�1���A��}��8W�{�	\����䊁'�ws���5�����FA����)���{�Q��ҹ+;�>�ĵ��]��!#p���ˍj�Vd�{i�Q�]����X^���Ϊ"�JgYEݨgM���Lk�>����N�lN�(��ٗ��2������Ld��=�n��''��`]����ʙX@^ ��"0��ނ�/��M,a�m�^�/�V��͚�ƗbX��\1q/	<�?'��bQ���Q���2uM�(�^��-�Z���O��ݒc��9��.�^Y۵�*�����F��b�P�iƭ�GSh��ĺ�K�{p���œx�DdC��}Eۓ@��>CI�k4�m�@�>���*_BC|4L��M�h6��|.~N�D߹m7MVq��f�Hέ�|��*�eU�l�9���W�U��p���yė[=7w�����@�8�ǉ�
(z���;/���?$�*�������ͨy)�vw���Q��*i����#�g)`(NMg��w̝��~���c��cy�'���,c��L�Xc"K�����2+��c��/Z�O'��;�_	<�����yuavx=��RJ�>2����}4��5��l��S���a�� ���*,���˕���H��mN���^<�W�4���V�>�T��9��0򩱑����0�[���tĊl�!�me�[/[H��bNm��;I��C��������f0���Ɠ��)~Q�M��<��G8;�}x��%9K�&3M��>��x�ӊ
�2/�����E�Y���̐N`_�W�Y��*�Ư������u��u|����,��oїos�����,l i�;xY�B��0Ey��S��w�q�%Ǟ�ƹ�á������x�
����2hnc�|{�5�gK	�P�ESSVt/�����& Ǵ���n'����3n��`��1���Fٓ��Cfn��g~�:��#��@����	��� r0�g
�=S����,~������=�ũ���5��&~�'��-H��&ɱ�o��y�����TJ���xt���	�����(�a�9b����4�v��ԑ�\�5��F)��Y��(�����84�S�MJ�*�r���ON�����_�R�`���aL�,Ё<!Ydټ�]J4U;.�o��f��.)��MN�@��i��:S�t�bg���ߛHn��V͛.<��}Q�=є�;B�Ԏ
��:�����C����ථ�	>�7x�?PK   ��Uŝ�;�  ?	  8   com/fillumina/maven/reverse/dependency/Association.class�U�SW�nYXV	���(h󨮥؇�XI��F��j�7�7ae���3�Wu�bՙ���M���YL�~�s��|�y�s����� �W�@R���A����.�m�4�G�g�R�c��l��`�a,R
��W��]�
�a�@SpN�y�2�nS�[�6-��M�+����n��pL�{��w����/�r��a ��DӐ���m���'d���o�5�8�	\T0��=L1�;DC��4&���,sO\f��\T�rK�e��Sq	I�\���)i�%�n������:f�j�����Q���$�m���\�a<{�(=�JOr=����PW4|�[2�-J��G!�s]����!���\��t+ӭk����nu�[��6�t�X�\U����y����m3J�2���dK�C`�X��'J�[i��=�H�w����UJ�)d?S���fMx��f�$SvnW�g�s,�-�a��\]/GF��#!�l��yj�S{O�'��U�es=�a�Wv��&ka�a�b�ټY3��A��aX�ߐg�kQ�*n�bŒ�Hw����"ђ�~���Cl��?��#�^�*�d��q�rv���#�ɂ׹A/v�a��}��;�بM����l�uw�V��3�g���g�lSo�;z[�]�QL��g~V)[?�4ǟhX�Z��5�^ɦ���_n�(
~c�z�*�o[׈4Ac-A�� ���
4�"�H�~�h-��1���D�%X��'/1�j�����B2@�yZ5�8�����X�} �IRj1�r(�&�����Zx���:�jtw���B��"j�*F|��Q��� !��x������V�ah2Fn���Ɨ�a�N�2�hF���1��e�J� ]�����>>܊B}�<�
�(�c�C�N�4��Z����lW腘v3X�7`iU�PL�g���}�-t(���2Ӆ������ҙ�w�~��I�[x����0��_�;��PK   ��U�w��v  .  6   com/fillumina/maven/reverse/dependency/ArgParser.class�V�s��]�c��&v��A���[�	�i�����!����t#m���+��-��4<��3-̄Zw&O��ԙ���;��ww˛L[�u?�=�s�=_w�|�q�~��	I)�����f�6�Z��We%H�Z��$��/j��L���K�=�\+[~�CC"�^v؉>��+n�tŲ�V�r�R�\�Nɓ���e�*�ҩJ��F!�s&i�@�iu�v�;:��cJ�.9�Iz�Kfq)�A��ɺ�=xHC��^��r�����o!�l�6%����xl����gr�.��R����R�T�]��`D`O�\��S1����Ε�N@����=6���c�j30�"�u��ɚ�V:g����kZ��������S��Ĕ��p�-[.*��@����V��Nh"�����2u�筫2̇�U�2���SsUɛ30�|��Cދ�q\�3T�vn�0��j�
�Y��^-,O6
�����6�ZhWYۤ�)355��U����'�B[c_0�M�S���S�/��y�!U`��:�D�Gtq�S�.�4���^�^��z��ۘ�ְ�~�R�ʼK1���s���ߙN�c���!����s�c�
�{I�{,����E�Wb2����(�.Y5�Z�e���g��	[�~��%]_�r��V��.��Uu��nŴ/����m���A�Iɠn��H��M
kz5bb,d�RI@�Ds�c)0++g�fh�����E߬�4k���ʰ�.�z<<��f��ݿ�i�Ot�+K�ԓ�]��Y�!�+�	�jX����$m���Yɖ�l�z�%��U�ɰ?��tҔ³�<㺁xf��n��K��coi��Ro���Qe9��lk�z�I������m3FBO��m鏔]w��|p�n^Xk���wa��Ӷ��@�nߦC^�*���H?�O�v�̉~I/�[����˚Էuu�k�a�W�!��������!�V3'��O;��lu�k������3>8�`V�8b�A6��wi�|��Q�n8Ϸg>-�|�=�g���_B��~��e�'��C���ϑ�	=1�������������uܝ�I擷�S���`z0�'|�O���h��y��>�p�������1T�k4š��H̭����Rb~�k3���l>sG�B��� R_r>��_�s����|&7*MLg�Q��G�~(����τ�tj:;��31����J����u�4lw�f�?���������b�n��АK/�!��٦st� �>�a>�#8����G��U��
���0X�Q��X\���eT$1&�YL~��|��"/
|ɗ��I�i��+����5��l����.�Okx��+\%P%�Q�K�z5H*���p�-jأ��q!����Z2�z���,�`1�^B��J�N�PJû�C�Q�5�ی%Ί���f?}�a}:��1�/ `�jx�F�D,x%\����h���w��1਀��ػ���X����n��X�����a���*�_3vv'�u�r��7��x�C,E�҆*�{D�^��pc3Ǉ�\Ҋ"3p�9�<�vl*�mR�6�� ��,�wW]JS]J������D��?u��ރϹ@��~���/��W���'���oXO"�PK   ��UӲ��  �  6   com/fillumina/maven/reverse/dependency/PackageId.class�U�S�F�ζ,��4	��$�e��6-�!mBK�Ԑ����>��Ȓ+���_���@�ڙ�vx��-3�鿔dORc4������������Xŷ)�W�С �0���y��v��pgW�<��-�6���\~{�T�t��a�洊OL��L�[|_�EW��#�u�v]ص��#^��Q�3��鶥���qV�\�n�$}Fǰ�Nq�3��W�K����T���tl����q�ε��Xs���^����m��p-w�U��*�II�Ã�Y4�2��uL�����R�/J7����&�4�+�UqU�5\?Q�`���JB]��+�B�u(��!�5�w(5c�~r_I��XPQԱ�����V�K���"�z�s�&��'�7��������֎p���%d95nmsה�P��&��T���YbHW=Zm�vȥR�6y���\D�Miý�-��ŝ^����_&"R%wvvOކ�Ƞ8^S�T0�y]�T��5�n��2��$����x�c��!��S�'q/��(�x {�K��7�L{���a���N�;��O����ܮ[�3[q��n��E<�:h����G��4����鉒���
��j�VI_Q��l����XtTl1\=�)TP�M�}+�}���J���I�D!�C4��p��g��K4~@�	��y���(A5掐6�0b��Cˎ����cL Ǹ a�wB��!��H�8B�"���8��	�$���#|�������/�K2�8��@�%f�P������A�zI����\ѥ�Jx�e"��2�S:�S�����&懞����0��2a��K�fiS$� x�����"��A��H�j$xd<	��7/�`��S�p�p ���k=�"n���W��Q�J��J��x�\�~?�E�oh�~�~b����R�U�3��A%_B�,2ד�j�js�π��|��(�
��b~K(TEFz725S��Y�HMk������^'{�7���&K��*��c\xPK   ��U<� ��  �  ?   com/fillumina/maven/reverse/dependency/AssociationBuilder.class��mSW��7��J���1(��bv\D$J�
(h�66�]�۰6��͆i�����۶S`�3��jg���鹻kFf����޻�>���s����o�x�qD�hPuDcx�w�U�N�z��RT<�ج�����1��0��ڞ]�����
ߎC�҇tgu|�~���f��ʮ����V���rŎp[ª�m�T�S�Κo���{v�Yh���pI�����B$R��:�p�!�hV׸�	�aHwMtEM|k�C���:.�KH4�K�Ei3h!�h\ƈ�QWp�a�.���׼&�V�x͒��N�!�1����8>�u7t|�1������^eK�N�k��-��7m����`�d`h0ud�{�����C�����tX� Ol��|���e�'�D��+�r���aJ�]L3�=t�DX��c����`��4�c�<yƑǌ�����;�⁎9|L��mB���c9!a�u,H�ˣ�+�5�{m��u�����Ͷ��w�_�#��̑7�ȁF�A]lV����#VۍM�>�u��^/sזߡQ��lʼ�`���Xc�fۭ�n�'?��(�vJ��GCʐOiy�D�+C��0L�&��*�o�F��������Nd�x��?�z��Grɖ��z�}K*�Ո�<T*�T��7$��RA�[��a��<�����cz/��{(d���`�h���E���b���"i��"���x��È��-�m���'�E
Oh���S|�=������=���u!���P�l���?I���.���n��m2${2�eO�!�
�����?�����V���q�H�2��<$�%���j �^Ƨ(���X�^�ԏA���җ�A��VF��K�U∏����@~FM�a���}ܟQ�Ѵ��
��*!><@�6����&E'��0H(�i�|̩@����`�C�(U�gX�=���3|N'!�`�`BCI��_�L�%�E_r�����s�1�h��z0!TRh�l_���� PK   ��UZ2�G  �  C   com/fillumina/maven/reverse/dependency/MavenReverseDependency.class�W�{W�ݼ&�$MA�BH7@���J��D7K$4���f�&�dvf;3_U|k��G-ԷV|?��l���k��g�S��Swv��d���'���s��w�=��3���1<A*T��F�@ㄜ�1[:c��#*��+8.P�4P+p �fb��m�2�#c9�������XZe��VNj&v�뗤y�h��ы�%'�g9c�A-���F4���y��֜M&�4����(;[�-�j���6�R�����C�Z�d��~�	�L�L\���v��S���S[���`'n3�j�v��\����7ۡ�h���D���g�D⸆�������'g�DЎJ�^�4u���t-:vD2n:g+�5��Y�"���#��i��4��֑�ւVD[~���x��FBqڵ��Kʌ�� 	C����qw����q�*�l�iʓ�N]s�vo�Nq5p��q����"�s\�I5h�W�8��U	+�T�����"�ob��6�J�ؖ����uR2�`�\��tn��\������j	}H.��a!��X��i��
��X,W��6��1p�� �ܽ΃��f�{J�L�L�nbe�O�69~���K[-uY6ǃ&.�<o2���D��u��M�-׉w8�ގ��nʒZ�d�ҵ�+�u&^�74���0ϩ15��^RD���Y)Nx��dBj���<�覠�R���������`�7aaB�J�,�<M��=�`>�sB(c�(s�	�Md��b��^�L��F�T�]fY����HJ��@�h=@����7cZ`C	��S����K�沔��-x����x;��YN�v�bs,Ma�[���Ļ\2�n���.�N6����r�������Jl/�@W��8�ާ��~�CѲs�eჰ|������|���&>���*�H&\�2`��M�?���8��rZTg��'|�ħ��B+-9�LC�����B��b� 1qW(eJDK�n��9�>#pl��4�8ٖN2�~��f�	�Q�\fDy���PS�MI{Pz�~/��q�Es�z�.�5NCi����!�[�5N������j�Z������� ��	��Lzct��̰ὁL�u8��]k�8h.�{&��y���-�qZ�0��$�LQ1�3�{)�����U��E�l�I�6����n������/2�演*���+�N��|�t݀�@f�T0����8�'𤁟�+�SmKI��)wRsQ����4������(X0zF:i^���;���W'�F�:7e�/��7�&��m����x�|%�Ը�YZNW��>�/�+�6�4~#�k��D�0�"GvY�Y������g\���N��CT���Q��ď0ۈ?pf��k6Tcw#�̫݋)�V�^�Q^���xp�&�g]h���εE9

��Xy�T�����߆|~�o1��N��� ��E>�gMH܀��i�|��u��)Ee1�u��%��¼�/��ql�C�,^ry�I�}�C����26�ڴ?���;�$�@���yt	N��\�#����(�R5���-U�O�{�]��[��p6�{+p������!���oԮ�a$�(j�����d:g��A��`!Sbh�_��{5�ЈT��b+����&~�6a7��ru-8�[q
;��m�@+ob��v�AJ=�=x�_}Wp�D����o�<:��'�����":�6J}�~�*��F��!|�`�6��o���-}���n1K���/`����8̷:����<[����~��U^,����˄���<�;�O���}a���0.��k���M������uR��s�~��Wd��D�ܿ���'Z�PK   ��U;脩�  ^  =   com/fillumina/maven/reverse/dependency/PomTreeExtractor.class�W�WW�=F��}AA�UZ�A[��ѐP�B[��LfҙI�����K���Һ���s�����7B����ɼ�������������p?ɨ�O�_�6T34�T�jPW��`|�&O9����9�|�]�j�]������R���Ȑ�E	��@=�错	�h���h�̨yn-��̓i��F���ਙIZ��KM9�Ű-�:s6�]�,!�Ш�	�53�e;4�g�2-�d��n	{�že=�J��0}S����/8��b�D���:��)�*'/�:�� �h|��p$���v���C+�H8���14�a�,n�z�t�ձ�":��^�j�|<�h�!ԩ�:W�@%��,�>�ꚬ�q�H�U�	��O�BEM�Z<��p���8�S�<��V���s:A��c�H�*y�p�S)Ȇ�g������:��kB��s
���UA�X��!��q���'�X\̈́�(Sb�awF��C��R�	͙������b�FgT�rt6$#�a	\Bd9HWS��g�H7�8у��^Q��&�c#��@2,� � �Q
��i;�w(C�H�E��;ME����	�\c
��>/�İJ��X��`�]q�az3��:d�t���A��Rb���)��)�ԍY���@^�53�]��c�u��P%L+H!�����o�S����T��*��3{+V�[B�5T'g��:֔��r��C�!3M����<��Ls+�N�7E͔����&�ޤߙ�Ș�ѧ��D�H��uZ�#F��vl�	+�����:�_�Y.��-�)ɍ��d��~E�����&���R��n
��	GM͏��eOθ�����*R�O�ʔçJ=����4��۟ҽ��d%�[��r��Y)^�-kc�W�t4MǦ��w�̴]_�Oe|��%|!j�m+�hFޜ�^����պ�K}�	@/�F��mQӜ�eC�3����b���Z�C��'4��j�5]�Ţ���2��"���!]�mN��C��ԗ�á�E�/���*0p��O<W�nk�5���~��G�+���=
���"�`Զ�C�v�w!�w���&}w��?����H)l����0`�� �Ӯ*j����vF��2��;�;k w����ǅ�U؆�kA���3X4�Sk{��Ow��/���=ӽ��q�
��h�c�� ֞������CÆ�P>�O��pZ��su:J� o��k�7\+�����	C������g���Ic��na���ыE���M�9U��MBk}�)Lܤ�	�4����.�v!{]���z�ҏ�<J�GY�L���<&����}��^���.��Ȯ�O�y��05��Lx�a��6��l�E� 7�r�;�@�_h=~#k�K"R��K���^�*�8ޫ���*�de�W��E��~1����7MQ�� ��~�̬��6��|��c7�>��W��%b���G��PK   ��U��s��  V  V   META-INF/maven/com.fillumina.maven.reverse.dependency/maven-reverse-dependency/pom.xml�SM��0��W��c�]$P�zŧڊ�.����4��ؑ�����t��g��̛������ �%���3��R�j�}��1�]�Y�j��"��f���mW�4� ���=`�+r�eC^�����g��x�ǫ�[��򰹹wĆ�B�$�2beB�Fqf�P}=�8�2&��.����Q%����4�
2�yP�U�~*)Wމ��!��h�.�Y:�~��{.�V��.�$B>$@���@~��`��V���G����V-�F`|�Txۉ��Fu�ÇtR.�Z�	��=�A']�*���E�e�{��*�'�NX!��̘o|�.��d�t�"��JEػ׫I�s܇.�"��[O@L"߼�l[����� ��Y�wa>=1�H��fw�<�I��Wx��#�$ Wr'�N�����8�0n&���=�,��-�{jun�aj��}�V�N�h-�� �����>:��riA/�D7�%�y��䫁�f�oEPK   ��U,�RM   _   ]   META-INF/maven/com.fillumina.maven.reverse.dependency/maven-reverse-dependency/pom.propertiesK,*�LKL.�L��M,K��-J-K-*N�MI-H�KI�K��J/�/- *H���K���)���K�+׃*�CR��ϳ5�3� PK
    ��U            	          �A    META-INF/PK   ��U���̆   �              ��'   META-INF/MANIFEST.MFPK
    ��U                      �A�   com/PK
    ��U                      �A  com/fillumina/PK
    ��U                      �A-  com/fillumina/maven/PK
    ��U                      �A_  com/fillumina/maven/reverse/PK
    ��U            '          �A�  com/fillumina/maven/reverse/dependency/PK
    ��U                      �A�  META-INF/maven/PK
    ��U            6          �A  META-INF/maven/com.fillumina.maven.reverse.dependency/PK
    ��U            O          �A_  META-INF/maven/com.fillumina.maven.reverse.dependency/maven-reverse-dependency/PK   ��UUՠ��  2  6           ���  com/fillumina/maven/reverse/dependency/PomLoader.classPK   ��Uŝ�;�  ?	  8           ���	  com/fillumina/maven/reverse/dependency/Association.classPK   ��U�w��v  .  6           ��*  com/fillumina/maven/reverse/dependency/ArgParser.classPK   ��UӲ��  �  6           ���  com/fillumina/maven/reverse/dependency/PackageId.classPK   ��U<� ��  �  ?           ��8  com/fillumina/maven/reverse/dependency/AssociationBuilder.classPK   ��UZ2�G  �  C           ��o  com/fillumina/maven/reverse/dependency/MavenReverseDependency.classPK   ��U;脩�  ^  =           ��%  com/fillumina/maven/reverse/dependency/PomTreeExtractor.classPK   ��U��s��  V  V           ��Y+  META-INF/maven/com.fillumina.maven.reverse.dependency/maven-reverse-dependency/pom.xmlPK   ��U,�RM   _   ]           ���-  META-INF/maven/com.fillumina.maven.reverse.dependency/maven-reverse-dependency/pom.propertiesPK      �  �.    