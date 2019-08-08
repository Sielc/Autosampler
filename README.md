# Autosampler serial protocol Rev. 1.02

## High level mode

Autosampler works as a finite state machine. All variables unsigned. Protocol is based on DOMP (Device Object Manager Protocol).

When turned on autosampler is in State = 101, preparing to become ready State = 0.

### Information parameters

Variable  Name		 Type		  Description

B1        State		[int]		 Describes current operation autosampler performs
```
  0 - Ready
 11 - Tray + Arm moving
 12 - Needle down
 13 - Syringe 
 14 - Home (Needle up, Arm back, Needle down)
 15 - Injection Start (Valve rotated)
 16 - Getting Ready (Valve back and needle up)
 21 - Washing
100 - Error occurred, ErrorCode = [what happened], waits for Command=0
101 - Getting ready when initializing or after aborted command. Finishes in State = 0
102 - Low level command was executed, use B3 = 0 to get ready
```
B2 ErrorCode		[int]		Describes an error, occurred while working
```
00000000 - no error
00000001 - tray not present
00000010 - tray rotation error
00000100 - arm rotation blocked
00001000 - needle moving error
00010000 - syringe moving error
00100000 - valve rotation error
2^32 - aborted
```

### Control parameters

Variable Name		Type		Description

B3 Command		[int]		Controls an autosampler, any low-level command will be interrupted.
```
0 - Get Ready	From any State != 0 it will cancel operation,
makes State = 101 & ErrorCode = 2^32, then back to State = 0 & ErrorCode = 0
1 - Injection 	If State == 0, then State = 11
2 - Wash needle	If State == 0, then State = 21, then State = 0
3 - Shaking
```

B4 1.Vial		[int]		Vial number

B5 1.Amount		[int] μL		Amount of sample in microliters

B6 1.ValveTime	[int] ms		How long to wait for sample injection. State = 15

B7 1.Depth		[int] mm		Needle offset from the highest possible needle position. 0 - highest / 45 - lowest. In millimeters.

B8 2.WashCycles	[int] 		Number of cycles

B9 3.ShakingMode	[int]		Specify shaking mode

B10 3.ShakingDuration


## Low level commands

Low level commands will work only when B1 State == [0, 100, 102] calling low-level command will change state to State = 102.

Arm, Tray - “E”
Variable	Name				Type	Units	Description

E1		Vial				[int]	-	Move tray and arm

Set E1			Choose vial
```
0        Go home. E1 = [0, 41] Will not work if needle F1 != 0
1-40		 Go to position 1-40
999		   Got to washing
10001    Recalibrate
10002    Abort
```
Get E1			Read current vial or state
```
0         Not moving at home
1-40      Not moving and vial is 1-40
999       Not moving on washing
20000-20040 Moving home or to vial 1-40
20999		  Moving to wash
1000x	    Error occurred
```
