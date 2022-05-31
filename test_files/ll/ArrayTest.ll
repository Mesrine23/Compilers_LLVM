

@.ArrayTest_vtable = global [0 x i8*] []


@.Test_vtable = global [1 x i8*] [
	i8* bitcast (i1 (i8*, i32)* @Test.start to i8*)
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

	%n = alloca i1

	%_0 = call i8* @calloc(i32 1, i32 8)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [1 x i8*], [1 x i8*]* @.Test_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	%_3 = bitcast i8* %_0 to i8***
	%_4 = load i8**, i8*** %_3
	%_5 = getelementptr i8*, i8** %_4, i32 0
	%_6 = load i8*, i8** %_5
	%_7 = bitcast i8* %_6 to i1 (i8*, i32)*
	%_8 = call i1 %_7(i8* %_0, i32 10)

	store i1 %_8, i1* %n

	ret i32 0

}

define i1 @Test.start(i8* %this, i32 %.sz) {
	%sz = alloca i32
	store i32 %.sz, i32* %sz
	%b = alloca i32*
	%l = alloca i32
	%i = alloca i32
	%_0 = load i32, i32* %sz
	%_1 = add i32 1, %_0
	%_2 = icmp sge i32 %_1, 1
	br i1 %_2, label %nsz_ok_0, label %nsz_err_0

	nsz_err_0:
	call void @throw_nsz()
	br label %nsz_ok_0

	nsz_ok_0:

	%_3 = call i8* @calloc(i32 %_1, i32 4)
	%_4 = bitcast i8* %_3 to i32*
	store i32 %_0, i32* %_4

	store i32* %_4, i32** %b

	%_5 = load i32*, i32** %b
	%_6 = load i32, i32* %_5
	store i32 %_6, i32* %l

	store i32 0, i32* %i

	br label %loop_check_0

	loop_check_0:
	%_7 = load i32, i32* %i
	%_8 = load i32, i32* %l
	%_9 = icmp slt i32 %_7, %_8
	br i1 %_9, label %loop_enter_0, label %loop_exit_0

	loop_enter_0:
	%_11 = load i32*, i32** %b
	%_12 = load i32, i32* %_11
	%_13 = load i32, i32* %i
	%_14 = icmp sge i32 %_13, 0
	%_15 = icmp slt i32 %_13, %_12
	%_16 = and i1 %_14, %_15
	br i1 %_16, label %oob_ok_0, label %oob_err_0

	oob_err_0:
	call void @throw_oob()
	br label %oob_ok_0

	oob_ok_0:
	%_17 = add i32 1, %_13
	%_18 = getelementptr i32, i32* %_11, i32 %_17
	%_19 = load i32, i32* %i
	store i32 %_19, i32* %_18

	%_20 = load i32, i32* %i
	%_21 = load i32*, i32** %b
	%_22 = load i32, i32* %_21
	%_23 = icmp sge i32 %_20, 0
	%_24 = icmp slt i32 %_20, %_22
	%_25 = and i1 %_23, %_24
	br i1 %_25, label %oob_ok_1, label %oob_err_1

	oob_err_1:
	call void @throw_oob()
	br label %oob_ok_1

	oob_ok_1:
	%_26 = add i32 1, %_20
	%_27 = getelementptr i32, i32* %_21, i32 %_26
	%_28 = load i32, i32* %_27
	call void (i32) @print_int(i32 %_28)

	%_29 = load i32, i32* %i
	%_30 = add i32 %_29, 1
	store i32 %_30, i32* %i

	br label %loop_check_0

	loop_exit_0:

	ret i1 1
}

