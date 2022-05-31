



@.Main_vtable = global [0 x i8*] []


@.ArrayTest_vtable = global [1 x i8*] [
	i8* bitcast (i32 (i8*, i32)* @ArrayTest.test to i8*)
]

@.B_vtable = global [1 x i8*] [
	i8* bitcast (i32 (i8*, i32)* @B.test to i8*)
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

	%ab = alloca i8*

	%_0 = call i8* @calloc(i32 1, i32 20)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [1 x i8*], [1 x i8*]* @.ArrayTest_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	store i8* %_0, i8** %ab

	%_3 = load i8*, i8** %ab
	%_4 = bitcast i8* %_3 to i8***
	%_5 = load i8**, i8*** %_4
	%_6 = getelementptr i8*, i8** %_5, i32 0
	%_7 = load i8*, i8** %_6
	%_8 = bitcast i8* %_7 to i32 (i8*, i32)*
	%_9 = call i32 %_8(i8* %_3, i32 3)

	call void (i32) @print_int(i32 %_9)

	ret i32 0

}

define i32 @ArrayTest.test(i8* %this, i32 %.num) {
	%num = alloca i32
	store i32 %.num, i32* %num
	%i = alloca i32
	%intArray = alloca i32*
	%_0 = load i32, i32* %num
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

	store i32* %_4, i32** %intArray

	%_5 = getelementptr i8, i8* %this, i32 16
	%_6 = bitcast i8* %_5 to i32*
	store i32 0, i32* %_6

	%_7 = getelementptr i8, i8* %this, i32 16
	%_8 = bitcast i8* %_7 to i32*
	%_9 = load i32, i32* %_8
	call void (i32) @print_int(i32 %_9)

	%_10 = load i32*, i32** %intArray
	%_11 = load i32, i32* %_10
	call void (i32) @print_int(i32 %_11)

	store i32 0, i32* %i

	call void (i32) @print_int(i32 111)

	br label %loop_check_0

	loop_check_0:
	%_12 = load i32, i32* %i
	%_13 = load i32*, i32** %intArray
	%_14 = load i32, i32* %_13
	%_15 = icmp slt i32 %_12, %_14
	br i1 %_15, label %loop_enter_0, label %loop_exit_0

	loop_enter_0:
	%_17 = load i32, i32* %i
	%_18 = add i32 %_17, 1
	call void (i32) @print_int(i32 %_18)

	%_19 = load i32*, i32** %intArray
	%_20 = load i32, i32* %_19
	%_21 = load i32, i32* %i
	%_22 = icmp sge i32 %_21, 0
	%_23 = icmp slt i32 %_21, %_20
	%_24 = and i1 %_22, %_23
	br i1 %_24, label %oob_ok_0, label %oob_err_0

	oob_err_0:
	call void @throw_oob()
	br label %oob_ok_0

	oob_ok_0:
	%_25 = add i32 1, %_21
	%_26 = getelementptr i32, i32* %_19, i32 %_25
	%_27 = load i32, i32* %i
	%_28 = add i32 %_27, 1
	store i32 %_28, i32* %_26

	%_29 = load i32, i32* %i
	%_30 = add i32 %_29, 1
	store i32 %_30, i32* %i

	br label %loop_check_0

	loop_exit_0:

	call void (i32) @print_int(i32 222)

	store i32 0, i32* %i

	br label %loop_check_1

	loop_check_1:
	%_31 = load i32, i32* %i
	%_32 = load i32*, i32** %intArray
	%_33 = load i32, i32* %_32
	%_34 = icmp slt i32 %_31, %_33
	br i1 %_34, label %loop_enter_1, label %loop_exit_1

	loop_enter_1:
	%_36 = load i32, i32* %i
	%_37 = load i32*, i32** %intArray
	%_38 = load i32, i32* %_37
	%_39 = icmp sge i32 %_36, 0
	%_40 = icmp slt i32 %_36, %_38
	%_41 = and i1 %_39, %_40
	br i1 %_41, label %oob_ok_1, label %oob_err_1

	oob_err_1:
	call void @throw_oob()
	br label %oob_ok_1

	oob_ok_1:
	%_42 = add i32 1, %_36
	%_43 = getelementptr i32, i32* %_37, i32 %_42
	%_44 = load i32, i32* %_43
	call void (i32) @print_int(i32 %_44)

	%_45 = load i32, i32* %i
	%_46 = add i32 %_45, 1
	store i32 %_46, i32* %i

	br label %loop_check_1

	loop_exit_1:

	call void (i32) @print_int(i32 333)

	%_47 = load i32*, i32** %intArray
	%_48 = load i32, i32* %_47
	ret i32 %_48
}

define i32 @B.test(i8* %this, i32 %.num) {
	%num = alloca i32
	store i32 %.num, i32* %num
	%i = alloca i32
	%intArray = alloca i32*
	%_0 = load i32, i32* %num
	%_1 = add i32 1, %_0
	%_2 = icmp sge i32 %_1, 1
	br i1 %_2, label %nsz_ok_1, label %nsz_err_1

	nsz_err_1:
	call void @throw_nsz()
	br label %nsz_ok_1

	nsz_ok_1:

	%_3 = call i8* @calloc(i32 %_1, i32 4)
	%_4 = bitcast i8* %_3 to i32*
	store i32 %_0, i32* %_4

	store i32* %_4, i32** %intArray

	%_5 = getelementptr i8, i8* %this, i32 20
	%_6 = bitcast i8* %_5 to i32*
	store i32 12, i32* %_6

	%_7 = getelementptr i8, i8* %this, i32 20
	%_8 = bitcast i8* %_7 to i32*
	%_9 = load i32, i32* %_8
	call void (i32) @print_int(i32 %_9)

	%_10 = load i32*, i32** %intArray
	%_11 = load i32, i32* %_10
	call void (i32) @print_int(i32 %_11)

	store i32 0, i32* %i

	call void (i32) @print_int(i32 111)

	br label %loop_check_2

	loop_check_2:
	%_12 = load i32, i32* %i
	%_13 = load i32*, i32** %intArray
	%_14 = load i32, i32* %_13
	%_15 = icmp slt i32 %_12, %_14
	br i1 %_15, label %loop_enter_2, label %loop_exit_2

	loop_enter_2:
	%_17 = load i32, i32* %i
	%_18 = add i32 %_17, 1
	call void (i32) @print_int(i32 %_18)

	%_19 = load i32*, i32** %intArray
	%_20 = load i32, i32* %_19
	%_21 = load i32, i32* %i
	%_22 = icmp sge i32 %_21, 0
	%_23 = icmp slt i32 %_21, %_20
	%_24 = and i1 %_22, %_23
	br i1 %_24, label %oob_ok_2, label %oob_err_2

	oob_err_2:
	call void @throw_oob()
	br label %oob_ok_2

	oob_ok_2:
	%_25 = add i32 1, %_21
	%_26 = getelementptr i32, i32* %_19, i32 %_25
	%_27 = load i32, i32* %i
	%_28 = add i32 %_27, 1
	store i32 %_28, i32* %_26

	%_29 = load i32, i32* %i
	%_30 = add i32 %_29, 1
	store i32 %_30, i32* %i

	br label %loop_check_2

	loop_exit_2:

	call void (i32) @print_int(i32 222)

	store i32 0, i32* %i

	br label %loop_check_3

	loop_check_3:
	%_31 = load i32, i32* %i
	%_32 = load i32*, i32** %intArray
	%_33 = load i32, i32* %_32
	%_34 = icmp slt i32 %_31, %_33
	br i1 %_34, label %loop_enter_3, label %loop_exit_3

	loop_enter_3:
	%_36 = load i32, i32* %i
	%_37 = load i32*, i32** %intArray
	%_38 = load i32, i32* %_37
	%_39 = icmp sge i32 %_36, 0
	%_40 = icmp slt i32 %_36, %_38
	%_41 = and i1 %_39, %_40
	br i1 %_41, label %oob_ok_3, label %oob_err_3

	oob_err_3:
	call void @throw_oob()
	br label %oob_ok_3

	oob_ok_3:
	%_42 = add i32 1, %_36
	%_43 = getelementptr i32, i32* %_37, i32 %_42
	%_44 = load i32, i32* %_43
	call void (i32) @print_int(i32 %_44)

	%_45 = load i32, i32* %i
	%_46 = add i32 %_45, 1
	store i32 %_46, i32* %i

	br label %loop_check_3

	loop_exit_3:

	call void (i32) @print_int(i32 333)

	%_47 = load i32*, i32** %intArray
	%_48 = load i32, i32* %_47
	ret i32 %_48
}

