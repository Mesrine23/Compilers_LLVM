

@.QuickSort_vtable = global [0 x i8*] []


@.QS_vtable = global [4 x i8*] [
	i8* bitcast (i32 (i8*, i32)* @QS.Start to i8*),
	i8* bitcast (i32 (i8*, i32, i32)* @QS.Sort to i8*),
	i8* bitcast (i32 (i8*)* @QS.Print to i8*),
	i8* bitcast (i32 (i8*, i32)* @QS.Init to i8*)
]


declare i8* @calloc(i32, i32)
declare i32 @printf(i8*, ...)
declare void @exit(i32)

@_cint = constant [4 x i8] c"%d\0a\00"
@_cOOB = constant [15 x i8] c"Out of bounds\0a\00"
@_cNSZ = constant [15 x i8] c"Negative size\0a\00"

define void @print_int(i32 %i) {
	%_str = bitcast [4 x i8]* @_cint to i8*
	call i32 (i8*, ...) @printf(i8* %_str, i32 %i)
	ret void
}

define void @throw_oob() {
	%_str = bitcast [15 x i8]* @_cOOB to i8*
	call i32 (i8*, ...) @printf(i8* %_str)
	call void @exit(i32 1)
	ret void
}

define void @throw_nsz() {
	%_str = bitcast [15 x i8]* @_cNSZ to i8*
	call i32 (i8*, ...) @printf(i8* %_str)
	call void @exit(i32 1)
	ret void
}

define i32 @main() {


	%_0 = call i8* @calloc(i32 1, i32 20)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [4 x i8*], [4 x i8*]* @.QS_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	%_3 = bitcast i8* %_0 to i8***
	%_4 = load i8**, i8*** %_3
	%_5 = getelementptr i8*, i8** %_4, i32 0
	%_6 = load i8*, i8** %_5
	%_7 = bitcast i8* %_6 to i32 (i8*, i32)*
	%_8 = call i32 %_7(i8* %_0, i32 10)

	call void (i32) @print_int(i32 %_8)

	ret i32 0

}

define i32 @QS.Start(i8* %this, i32 %.sz) {
	%sz = alloca i32
	store i32 %.sz, i32* %sz
	%aux01 = alloca i32
	%_0 = bitcast i8* %this to i8***
	%_1 = load i8**, i8*** %_0
	%_2 = getelementptr i8*, i8** %_1, i32 3
	%_3 = load i8*, i8** %_2
	%_4 = bitcast i8* %_3 to i32 (i8*, i32)*
	%_5 = load i32, i32* %sz
	%_6 = call i32 %_4(i8* %this, i32 %_5)

	store i32 %_6, i32* %aux01

	%_7 = bitcast i8* %this to i8***
	%_8 = load i8**, i8*** %_7
	%_9 = getelementptr i8*, i8** %_8, i32 2
	%_10 = load i8*, i8** %_9
	%_11 = bitcast i8* %_10 to i32 (i8*)*
	%_12 = call i32 %_11(i8* %this)

	store i32 %_12, i32* %aux01

	call void (i32) @print_int(i32 9999)

	%_13 = getelementptr i8, i8* %this, i32 16
	%_14 = bitcast i8* %_13 to i32*
	%_15 = load i32, i32* %_14
	%_16 = sub i32 %_15, 1
	store i32 %_16, i32* %aux01

	%_17 = bitcast i8* %this to i8***
	%_18 = load i8**, i8*** %_17
	%_19 = getelementptr i8*, i8** %_18, i32 1
	%_20 = load i8*, i8** %_19
	%_21 = bitcast i8* %_20 to i32 (i8*, i32, i32)*
	%_22 = load i32, i32* %aux01
	%_23 = call i32 %_21(i8* %this, i32 0, i32 %_22)

	store i32 %_23, i32* %aux01

	%_24 = bitcast i8* %this to i8***
	%_25 = load i8**, i8*** %_24
	%_26 = getelementptr i8*, i8** %_25, i32 2
	%_27 = load i8*, i8** %_26
	%_28 = bitcast i8* %_27 to i32 (i8*)*
	%_29 = call i32 %_28(i8* %this)

	store i32 %_29, i32* %aux01

	ret i32 0
}

define i32 @QS.Sort(i8* %this, i32 %.left, i32 %.right) {
	%left = alloca i32
	store i32 %.left, i32* %left
	%right = alloca i32
	store i32 %.right, i32* %right
	%v = alloca i32
	%i = alloca i32
	%j = alloca i32
	%nt = alloca i32
	%t = alloca i32
	%cont01 = alloca i1
	%cont02 = alloca i1
	%aux03 = alloca i32
	store i32 0, i32* %t

	%_0 = load i32, i32* %left
	%_1 = load i32, i32* %right
	%_2 = icmp slt i32 %_0, %_1
	br i1 %_2, label %if_then_0, label %if_else_0

	if_else_0:
	store i32 0, i32* %nt

	br label %if_end_0

	if_then_0:
	%_3 = load i32, i32* %right
	%_4 = getelementptr i8, i8* %this, i32 8
	%_5 = bitcast i8* %_4 to i32**
	%_6 = load i32*, i32** %_5
	%_7 = load i32, i32* %_6
	%_8 = icmp sge i32 %_3, 0
	%_9 = icmp slt i32 %_3, %_7
	%_10 = and i1 %_8, %_9
	br i1 %_10, label %oob_ok_0, label %oob_err_0

	oob_err_0:
	call void @throw_oob()
	br label %oob_ok_0

	oob_ok_0:
	%_11 = add i32 1, %_3
	%_12 = getelementptr i32, i32* %_6, i32 %_11
	%_13 = load i32, i32* %_12
	store i32 %_13, i32* %v

	%_14 = load i32, i32* %left
	%_15 = sub i32 %_14, 1
	store i32 %_15, i32* %i

	%_16 = load i32, i32* %right
	store i32 %_16, i32* %j

	store i1 1, i1* %cont01

	br label %loop_check_0

	loop_check_0:
	%_17 = load i1, i1* %cont01
	br i1 %_17, label %loop_enter_0, label %loop_exit_0

	loop_enter_0:
	store i1 1, i1* %cont02

	br label %loop_check_1

	loop_check_1:
	%_19 = load i1, i1* %cont02
	br i1 %_19, label %loop_enter_1, label %loop_exit_1

	loop_enter_1:
	%_21 = load i32, i32* %i
	%_22 = add i32 %_21, 1
	store i32 %_22, i32* %i

	%_23 = load i32, i32* %i
	%_24 = getelementptr i8, i8* %this, i32 8
	%_25 = bitcast i8* %_24 to i32**
	%_26 = load i32*, i32** %_25
	%_27 = load i32, i32* %_26
	%_28 = icmp sge i32 %_23, 0
	%_29 = icmp slt i32 %_23, %_27
	%_30 = and i1 %_28, %_29
	br i1 %_30, label %oob_ok_1, label %oob_err_1

	oob_err_1:
	call void @throw_oob()
	br label %oob_ok_1

	oob_ok_1:
	%_31 = add i32 1, %_23
	%_32 = getelementptr i32, i32* %_26, i32 %_31
	%_33 = load i32, i32* %_32
	store i32 %_33, i32* %aux03

	%_34 = load i32, i32* %aux03
	%_35 = load i32, i32* %v
	%_36 = icmp slt i32 %_34, %_35
	%_37 = xor i1 %_36, 1
	br i1 %_37, label %if_then_1, label %if_else_1

	if_else_1:
	store i1 1, i1* %cont02

	br label %if_end_1

	if_then_1:
	store i1 0, i1* %cont02

	br label %if_end_1

	if_end_1:

	br label %loop_check_1

	loop_exit_1:

	store i1 1, i1* %cont02

	br label %loop_check_2

	loop_check_2:
	%_38 = load i1, i1* %cont02
	br i1 %_38, label %loop_enter_2, label %loop_exit_2

	loop_enter_2:
	%_40 = load i32, i32* %j
	%_41 = sub i32 %_40, 1
	store i32 %_41, i32* %j

	%_42 = load i32, i32* %j
	%_43 = getelementptr i8, i8* %this, i32 8
	%_44 = bitcast i8* %_43 to i32**
	%_45 = load i32*, i32** %_44
	%_46 = load i32, i32* %_45
	%_47 = icmp sge i32 %_42, 0
	%_48 = icmp slt i32 %_42, %_46
	%_49 = and i1 %_47, %_48
	br i1 %_49, label %oob_ok_2, label %oob_err_2

	oob_err_2:
	call void @throw_oob()
	br label %oob_ok_2

	oob_ok_2:
	%_50 = add i32 1, %_42
	%_51 = getelementptr i32, i32* %_45, i32 %_50
	%_52 = load i32, i32* %_51
	store i32 %_52, i32* %aux03

	%_53 = load i32, i32* %v
	%_54 = load i32, i32* %aux03
	%_55 = icmp slt i32 %_53, %_54
	%_56 = xor i1 %_55, 1
	br i1 %_56, label %if_then_2, label %if_else_2

	if_else_2:
	store i1 1, i1* %cont02

	br label %if_end_2

	if_then_2:
	store i1 0, i1* %cont02

	br label %if_end_2

	if_end_2:

	br label %loop_check_2

	loop_exit_2:

	%_57 = load i32, i32* %i
	%_58 = getelementptr i8, i8* %this, i32 8
	%_59 = bitcast i8* %_58 to i32**
	%_60 = load i32*, i32** %_59
	%_61 = load i32, i32* %_60
	%_62 = icmp sge i32 %_57, 0
	%_63 = icmp slt i32 %_57, %_61
	%_64 = and i1 %_62, %_63
	br i1 %_64, label %oob_ok_3, label %oob_err_3

	oob_err_3:
	call void @throw_oob()
	br label %oob_ok_3

	oob_ok_3:
	%_65 = add i32 1, %_57
	%_66 = getelementptr i32, i32* %_60, i32 %_65
	%_67 = load i32, i32* %_66
	store i32 %_67, i32* %t

	%_68 = getelementptr i8, i8* %this, i32 8
	%_69 = bitcast i8* %_68 to i32**
	%_70 = load i32*, i32** %_69
	%_71 = load i32, i32* %_70
	%_72 = load i32, i32* %i
	%_73 = icmp sge i32 %_72, 0
	%_74 = icmp slt i32 %_72, %_71
	%_75 = and i1 %_73, %_74
	br i1 %_75, label %oob_ok_4, label %oob_err_4

	oob_err_4:
	call void @throw_oob()
	br label %oob_ok_4

	oob_ok_4:
	%_76 = add i32 1, %_72
	%_77 = getelementptr i32, i32* %_70, i32 %_76
	%_78 = load i32, i32* %j
	%_79 = getelementptr i8, i8* %this, i32 8
	%_80 = bitcast i8* %_79 to i32**
	%_81 = load i32*, i32** %_80
	%_82 = load i32, i32* %_81
	%_83 = icmp sge i32 %_78, 0
	%_84 = icmp slt i32 %_78, %_82
	%_85 = and i1 %_83, %_84
	br i1 %_85, label %oob_ok_5, label %oob_err_5

	oob_err_5:
	call void @throw_oob()
	br label %oob_ok_5

	oob_ok_5:
	%_86 = add i32 1, %_78
	%_87 = getelementptr i32, i32* %_81, i32 %_86
	%_88 = load i32, i32* %_87
	store i32 %_88, i32* %_77

	%_89 = getelementptr i8, i8* %this, i32 8
	%_90 = bitcast i8* %_89 to i32**
	%_91 = load i32*, i32** %_90
	%_92 = load i32, i32* %_91
	%_93 = load i32, i32* %j
	%_94 = icmp sge i32 %_93, 0
	%_95 = icmp slt i32 %_93, %_92
	%_96 = and i1 %_94, %_95
	br i1 %_96, label %oob_ok_6, label %oob_err_6

	oob_err_6:
	call void @throw_oob()
	br label %oob_ok_6

	oob_ok_6:
	%_97 = add i32 1, %_93
	%_98 = getelementptr i32, i32* %_91, i32 %_97
	%_99 = load i32, i32* %t
	store i32 %_99, i32* %_98

	%_100 = load i32, i32* %j
	%_101 = load i32, i32* %i
	%_102 = add i32 %_101, 1
	%_103 = icmp slt i32 %_100, %_102
	br i1 %_103, label %if_then_3, label %if_else_3

	if_else_3:
	store i1 1, i1* %cont01

	br label %if_end_3

	if_then_3:
	store i1 0, i1* %cont01

	br label %if_end_3

	if_end_3:

	br label %loop_check_0

	loop_exit_0:

	%_104 = getelementptr i8, i8* %this, i32 8
	%_105 = bitcast i8* %_104 to i32**
	%_106 = load i32*, i32** %_105
	%_107 = load i32, i32* %_106
	%_108 = load i32, i32* %j
	%_109 = icmp sge i32 %_108, 0
	%_110 = icmp slt i32 %_108, %_107
	%_111 = and i1 %_109, %_110
	br i1 %_111, label %oob_ok_7, label %oob_err_7

	oob_err_7:
	call void @throw_oob()
	br label %oob_ok_7

	oob_ok_7:
	%_112 = add i32 1, %_108
	%_113 = getelementptr i32, i32* %_106, i32 %_112
	%_114 = load i32, i32* %i
	%_115 = getelementptr i8, i8* %this, i32 8
	%_116 = bitcast i8* %_115 to i32**
	%_117 = load i32*, i32** %_116
	%_118 = load i32, i32* %_117
	%_119 = icmp sge i32 %_114, 0
	%_120 = icmp slt i32 %_114, %_118
	%_121 = and i1 %_119, %_120
	br i1 %_121, label %oob_ok_8, label %oob_err_8

	oob_err_8:
	call void @throw_oob()
	br label %oob_ok_8

	oob_ok_8:
	%_122 = add i32 1, %_114
	%_123 = getelementptr i32, i32* %_117, i32 %_122
	%_124 = load i32, i32* %_123
	store i32 %_124, i32* %_113

	%_125 = getelementptr i8, i8* %this, i32 8
	%_126 = bitcast i8* %_125 to i32**
	%_127 = load i32*, i32** %_126
	%_128 = load i32, i32* %_127
	%_129 = load i32, i32* %i
	%_130 = icmp sge i32 %_129, 0
	%_131 = icmp slt i32 %_129, %_128
	%_132 = and i1 %_130, %_131
	br i1 %_132, label %oob_ok_9, label %oob_err_9

	oob_err_9:
	call void @throw_oob()
	br label %oob_ok_9

	oob_ok_9:
	%_133 = add i32 1, %_129
	%_134 = getelementptr i32, i32* %_127, i32 %_133
	%_135 = load i32, i32* %right
	%_136 = getelementptr i8, i8* %this, i32 8
	%_137 = bitcast i8* %_136 to i32**
	%_138 = load i32*, i32** %_137
	%_139 = load i32, i32* %_138
	%_140 = icmp sge i32 %_135, 0
	%_141 = icmp slt i32 %_135, %_139
	%_142 = and i1 %_140, %_141
	br i1 %_142, label %oob_ok_10, label %oob_err_10

	oob_err_10:
	call void @throw_oob()
	br label %oob_ok_10

	oob_ok_10:
	%_143 = add i32 1, %_135
	%_144 = getelementptr i32, i32* %_138, i32 %_143
	%_145 = load i32, i32* %_144
	store i32 %_145, i32* %_134

	%_146 = getelementptr i8, i8* %this, i32 8
	%_147 = bitcast i8* %_146 to i32**
	%_148 = load i32*, i32** %_147
	%_149 = load i32, i32* %_148
	%_150 = load i32, i32* %right
	%_151 = icmp sge i32 %_150, 0
	%_152 = icmp slt i32 %_150, %_149
	%_153 = and i1 %_151, %_152
	br i1 %_153, label %oob_ok_11, label %oob_err_11

	oob_err_11:
	call void @throw_oob()
	br label %oob_ok_11

	oob_ok_11:
	%_154 = add i32 1, %_150
	%_155 = getelementptr i32, i32* %_148, i32 %_154
	%_156 = load i32, i32* %t
	store i32 %_156, i32* %_155

	%_157 = bitcast i8* %this to i8***
	%_158 = load i8**, i8*** %_157
	%_159 = getelementptr i8*, i8** %_158, i32 1
	%_160 = load i8*, i8** %_159
	%_161 = bitcast i8* %_160 to i32 (i8*, i32, i32)*
	%_162 = load i32, i32* %i
	%_163 = sub i32 %_162, 1
	%_164 = load i32, i32* %left
	%_165 = call i32 %_161(i8* %this, i32 %_164, i32 %_163)

	store i32 %_165, i32* %nt

	%_166 = bitcast i8* %this to i8***
	%_167 = load i8**, i8*** %_166
	%_168 = getelementptr i8*, i8** %_167, i32 1
	%_169 = load i8*, i8** %_168
	%_170 = bitcast i8* %_169 to i32 (i8*, i32, i32)*
	%_171 = load i32, i32* %i
	%_172 = add i32 %_171, 1
	%_173 = load i32, i32* %right
	%_174 = call i32 %_170(i8* %this, i32 %_172, i32 %_173)

	store i32 %_174, i32* %nt

	br label %if_end_0

	if_end_0:

	ret i32 0
}

define i32 @QS.Print(i8* %this) {
	%j = alloca i32
	store i32 0, i32* %j

	br label %loop_check_3

	loop_check_3:
	%_0 = load i32, i32* %j
	%_1 = getelementptr i8, i8* %this, i32 16
	%_2 = bitcast i8* %_1 to i32*
	%_3 = load i32, i32* %_2
	%_4 = icmp slt i32 %_0, %_3
	br i1 %_4, label %loop_enter_3, label %loop_exit_3

	loop_enter_3:
	%_6 = load i32, i32* %j
	%_7 = getelementptr i8, i8* %this, i32 8
	%_8 = bitcast i8* %_7 to i32**
	%_9 = load i32*, i32** %_8
	%_10 = load i32, i32* %_9
	%_11 = icmp sge i32 %_6, 0
	%_12 = icmp slt i32 %_6, %_10
	%_13 = and i1 %_11, %_12
	br i1 %_13, label %oob_ok_12, label %oob_err_12

	oob_err_12:
	call void @throw_oob()
	br label %oob_ok_12

	oob_ok_12:
	%_14 = add i32 1, %_6
	%_15 = getelementptr i32, i32* %_9, i32 %_14
	%_16 = load i32, i32* %_15
	call void (i32) @print_int(i32 %_16)

	%_17 = load i32, i32* %j
	%_18 = add i32 %_17, 1
	store i32 %_18, i32* %j

	br label %loop_check_3

	loop_exit_3:

	ret i32 0
}

define i32 @QS.Init(i8* %this, i32 %.sz) {
	%sz = alloca i32
	store i32 %.sz, i32* %sz
	%_0 = load i32, i32* %sz
	%_1 = getelementptr i8, i8* %this, i32 16
	%_2 = bitcast i8* %_1 to i32*
	store i32 %_0, i32* %_2

	%_3 = load i32, i32* %sz
	%_4 = add i32 1, %_3
	%_5 = icmp sge i32 %_4, 1
	br i1 %_5, label %nsz_ok_0, label %nsz_err_0

	nsz_err_0:
	call void @throw_nsz()
	br label %nsz_ok_0

	nsz_ok_0:

	%_6 = call i8* @calloc(i32 %_4, i32 4)
	%_7 = bitcast i8* %_6 to i32*
	store i32 %_3, i32* %_7

	%_8 = getelementptr i8, i8* %this, i32 8
	%_9 = bitcast i8* %_8 to i32**
	store i32* %_7, i32** %_9

	%_10 = getelementptr i8, i8* %this, i32 8
	%_11 = bitcast i8* %_10 to i32**
	%_12 = load i32*, i32** %_11
	%_13 = load i32, i32* %_12
	%_14 = icmp sge i32 0, 0
	%_15 = icmp slt i32 0, %_13
	%_16 = and i1 %_14, %_15
	br i1 %_16, label %oob_ok_13, label %oob_err_13

	oob_err_13:
	call void @throw_oob()
	br label %oob_ok_13

	oob_ok_13:
	%_17 = add i32 1, 0
	%_18 = getelementptr i32, i32* %_12, i32 %_17
	store i32 20, i32* %_18

	%_19 = getelementptr i8, i8* %this, i32 8
	%_20 = bitcast i8* %_19 to i32**
	%_21 = load i32*, i32** %_20
	%_22 = load i32, i32* %_21
	%_23 = icmp sge i32 1, 0
	%_24 = icmp slt i32 1, %_22
	%_25 = and i1 %_23, %_24
	br i1 %_25, label %oob_ok_14, label %oob_err_14

	oob_err_14:
	call void @throw_oob()
	br label %oob_ok_14

	oob_ok_14:
	%_26 = add i32 1, 1
	%_27 = getelementptr i32, i32* %_21, i32 %_26
	store i32 7, i32* %_27

	%_28 = getelementptr i8, i8* %this, i32 8
	%_29 = bitcast i8* %_28 to i32**
	%_30 = load i32*, i32** %_29
	%_31 = load i32, i32* %_30
	%_32 = icmp sge i32 2, 0
	%_33 = icmp slt i32 2, %_31
	%_34 = and i1 %_32, %_33
	br i1 %_34, label %oob_ok_15, label %oob_err_15

	oob_err_15:
	call void @throw_oob()
	br label %oob_ok_15

	oob_ok_15:
	%_35 = add i32 1, 2
	%_36 = getelementptr i32, i32* %_30, i32 %_35
	store i32 12, i32* %_36

	%_37 = getelementptr i8, i8* %this, i32 8
	%_38 = bitcast i8* %_37 to i32**
	%_39 = load i32*, i32** %_38
	%_40 = load i32, i32* %_39
	%_41 = icmp sge i32 3, 0
	%_42 = icmp slt i32 3, %_40
	%_43 = and i1 %_41, %_42
	br i1 %_43, label %oob_ok_16, label %oob_err_16

	oob_err_16:
	call void @throw_oob()
	br label %oob_ok_16

	oob_ok_16:
	%_44 = add i32 1, 3
	%_45 = getelementptr i32, i32* %_39, i32 %_44
	store i32 18, i32* %_45

	%_46 = getelementptr i8, i8* %this, i32 8
	%_47 = bitcast i8* %_46 to i32**
	%_48 = load i32*, i32** %_47
	%_49 = load i32, i32* %_48
	%_50 = icmp sge i32 4, 0
	%_51 = icmp slt i32 4, %_49
	%_52 = and i1 %_50, %_51
	br i1 %_52, label %oob_ok_17, label %oob_err_17

	oob_err_17:
	call void @throw_oob()
	br label %oob_ok_17

	oob_ok_17:
	%_53 = add i32 1, 4
	%_54 = getelementptr i32, i32* %_48, i32 %_53
	store i32 2, i32* %_54

	%_55 = getelementptr i8, i8* %this, i32 8
	%_56 = bitcast i8* %_55 to i32**
	%_57 = load i32*, i32** %_56
	%_58 = load i32, i32* %_57
	%_59 = icmp sge i32 5, 0
	%_60 = icmp slt i32 5, %_58
	%_61 = and i1 %_59, %_60
	br i1 %_61, label %oob_ok_18, label %oob_err_18

	oob_err_18:
	call void @throw_oob()
	br label %oob_ok_18

	oob_ok_18:
	%_62 = add i32 1, 5
	%_63 = getelementptr i32, i32* %_57, i32 %_62
	store i32 11, i32* %_63

	%_64 = getelementptr i8, i8* %this, i32 8
	%_65 = bitcast i8* %_64 to i32**
	%_66 = load i32*, i32** %_65
	%_67 = load i32, i32* %_66
	%_68 = icmp sge i32 6, 0
	%_69 = icmp slt i32 6, %_67
	%_70 = and i1 %_68, %_69
	br i1 %_70, label %oob_ok_19, label %oob_err_19

	oob_err_19:
	call void @throw_oob()
	br label %oob_ok_19

	oob_ok_19:
	%_71 = add i32 1, 6
	%_72 = getelementptr i32, i32* %_66, i32 %_71
	store i32 6, i32* %_72

	%_73 = getelementptr i8, i8* %this, i32 8
	%_74 = bitcast i8* %_73 to i32**
	%_75 = load i32*, i32** %_74
	%_76 = load i32, i32* %_75
	%_77 = icmp sge i32 7, 0
	%_78 = icmp slt i32 7, %_76
	%_79 = and i1 %_77, %_78
	br i1 %_79, label %oob_ok_20, label %oob_err_20

	oob_err_20:
	call void @throw_oob()
	br label %oob_ok_20

	oob_ok_20:
	%_80 = add i32 1, 7
	%_81 = getelementptr i32, i32* %_75, i32 %_80
	store i32 9, i32* %_81

	%_82 = getelementptr i8, i8* %this, i32 8
	%_83 = bitcast i8* %_82 to i32**
	%_84 = load i32*, i32** %_83
	%_85 = load i32, i32* %_84
	%_86 = icmp sge i32 8, 0
	%_87 = icmp slt i32 8, %_85
	%_88 = and i1 %_86, %_87
	br i1 %_88, label %oob_ok_21, label %oob_err_21

	oob_err_21:
	call void @throw_oob()
	br label %oob_ok_21

	oob_ok_21:
	%_89 = add i32 1, 8
	%_90 = getelementptr i32, i32* %_84, i32 %_89
	store i32 19, i32* %_90

	%_91 = getelementptr i8, i8* %this, i32 8
	%_92 = bitcast i8* %_91 to i32**
	%_93 = load i32*, i32** %_92
	%_94 = load i32, i32* %_93
	%_95 = icmp sge i32 9, 0
	%_96 = icmp slt i32 9, %_94
	%_97 = and i1 %_95, %_96
	br i1 %_97, label %oob_ok_22, label %oob_err_22

	oob_err_22:
	call void @throw_oob()
	br label %oob_ok_22

	oob_ok_22:
	%_98 = add i32 1, 9
	%_99 = getelementptr i32, i32* %_93, i32 %_98
	store i32 5, i32* %_99

	ret i32 0
}

