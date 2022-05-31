

@.test15_vtable = global [0 x i8*] []


@.Test_vtable = global [3 x i8*] [
	i8* bitcast (i32 (i8*)* @Test.start to i8*),
	i8* bitcast (i32 (i8*)* @Test.mutual1 to i8*),
	i8* bitcast (i32 (i8*)* @Test.mutual2 to i8*)
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


	%_0 = call i8* @calloc(i32 1, i32 16)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [3 x i8*], [3 x i8*]* @.Test_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	%_3 = bitcast i8* %_0 to i8***
	%_4 = load i8**, i8*** %_3
	%_5 = getelementptr i8*, i8** %_4, i32 0
	%_6 = load i8*, i8** %_5
	%_7 = bitcast i8* %_6 to i32 (i8*)*
	%_8 = call i32 %_7(i8* %_0)

	call void (i32) @print_int(i32 %_8)

	ret i32 0

}

define i32 @Test.start(i8* %this) {
	%_0 = getelementptr i8, i8* %this, i32 8
	%_1 = bitcast i8* %_0 to i32*
	store i32 4, i32* %_1

	%_2 = getelementptr i8, i8* %this, i32 12
	%_3 = bitcast i8* %_2 to i32*
	store i32 0, i32* %_3

	%_4 = bitcast i8* %this to i8***
	%_5 = load i8**, i8*** %_4
	%_6 = getelementptr i8*, i8** %_5, i32 1
	%_7 = load i8*, i8** %_6
	%_8 = bitcast i8* %_7 to i32 (i8*)*
	%_9 = call i32 %_8(i8* %this)

	ret i32 %_9
}

define i32 @Test.mutual1(i8* %this) {
	%j = alloca i32
	%_0 = getelementptr i8, i8* %this, i32 8
	%_1 = bitcast i8* %_0 to i32*
	%_2 = load i32, i32* %_1
	%_3 = sub i32 %_2, 1
	%_4 = getelementptr i8, i8* %this, i32 8
	%_5 = bitcast i8* %_4 to i32*
	store i32 %_3, i32* %_5

	%_6 = getelementptr i8, i8* %this, i32 8
	%_7 = bitcast i8* %_6 to i32*
	%_8 = load i32, i32* %_7
	%_9 = icmp slt i32 %_8, 0
	br i1 %_9, label %if_then_0, label %if_else_0

	if_else_0:
	%_10 = getelementptr i8, i8* %this, i32 12
	%_11 = bitcast i8* %_10 to i32*
	%_12 = load i32, i32* %_11
	call void (i32) @print_int(i32 %_12)

	%_13 = getelementptr i8, i8* %this, i32 12
	%_14 = bitcast i8* %_13 to i32*
	store i32 1, i32* %_14

	%_15 = bitcast i8* %this to i8***
	%_16 = load i8**, i8*** %_15
	%_17 = getelementptr i8*, i8** %_16, i32 2
	%_18 = load i8*, i8** %_17
	%_19 = bitcast i8* %_18 to i32 (i8*)*
	%_20 = call i32 %_19(i8* %this)

	store i32 %_20, i32* %j

	br label %if_end_0

	if_then_0:
	%_21 = getelementptr i8, i8* %this, i32 12
	%_22 = bitcast i8* %_21 to i32*
	store i32 0, i32* %_22

	br label %if_end_0

	if_end_0:

	%_23 = getelementptr i8, i8* %this, i32 12
	%_24 = bitcast i8* %_23 to i32*
	%_25 = load i32, i32* %_24
	ret i32 %_25
}

define i32 @Test.mutual2(i8* %this) {
	%j = alloca i32
	%_0 = getelementptr i8, i8* %this, i32 8
	%_1 = bitcast i8* %_0 to i32*
	%_2 = load i32, i32* %_1
	%_3 = sub i32 %_2, 1
	%_4 = getelementptr i8, i8* %this, i32 8
	%_5 = bitcast i8* %_4 to i32*
	store i32 %_3, i32* %_5

	%_6 = getelementptr i8, i8* %this, i32 8
	%_7 = bitcast i8* %_6 to i32*
	%_8 = load i32, i32* %_7
	%_9 = icmp slt i32 %_8, 0
	br i1 %_9, label %if_then_1, label %if_else_1

	if_else_1:
	%_10 = getelementptr i8, i8* %this, i32 12
	%_11 = bitcast i8* %_10 to i32*
	%_12 = load i32, i32* %_11
	call void (i32) @print_int(i32 %_12)

	%_13 = getelementptr i8, i8* %this, i32 12
	%_14 = bitcast i8* %_13 to i32*
	store i32 0, i32* %_14

	%_15 = bitcast i8* %this to i8***
	%_16 = load i8**, i8*** %_15
	%_17 = getelementptr i8*, i8** %_16, i32 1
	%_18 = load i8*, i8** %_17
	%_19 = bitcast i8* %_18 to i32 (i8*)*
	%_20 = call i32 %_19(i8* %this)

	store i32 %_20, i32* %j

	br label %if_end_1

	if_then_1:
	%_21 = getelementptr i8, i8* %this, i32 12
	%_22 = bitcast i8* %_21 to i32*
	store i32 0, i32* %_22

	br label %if_end_1

	if_end_1:

	%_23 = getelementptr i8, i8* %this, i32 12
	%_24 = bitcast i8* %_23 to i32*
	%_25 = load i32, i32* %_24
	ret i32 %_25
}

