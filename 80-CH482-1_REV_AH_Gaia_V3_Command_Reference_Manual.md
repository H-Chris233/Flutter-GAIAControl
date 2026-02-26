# GAIA v3 Command

# Reference Manual

80-CH482-1 Rev. AH 

April 14, 2022 

For additional information or to submit technical questions, go to https://createpoint.qti.qualcomm.com 

Confidential – Qualcomm Technologies, Inc. and/or its affiliated companies – May Contain Trade Secrets 

NO PUBLIC DISCLOSURE PERMITTED: Please report postings of this document on public servers or websites to DocCtrlAgent@qualcomm.com. 

Confidential Distribution: Use or distribution of this item, in whole or in part, is prohibited except as expressly permitted by written agreement(s) and/or terms with Qualcomm Incorporated and/or its subsidiaries. 

Not to be used, copied, reproduced, or modified in whole or in part, nor its contents revealed in any manner to others without the express written permission of Qualcomm Technologies International, Ltd. 

All Qualcomm products mentioned herein are products of Qualcomm Technologies, Inc. and/or its subsidiaries. 

Qualcomm is a trademark or registered trademark of Qualcomm Incorporated. Other product and brand names may be trademarks or registered trademarks of their respective owners. 

This technical data may be subject to U.S. and international export, re-export, or transfer ("export") laws. Diversion contrary to U.S. and international law is strictly prohibited. 

Qualcomm Technologies International, Ltd. is a company registered in England and Wales 

with a registered office at: Churchill House, Cambridge Business Park, Cowley Road, 

Cambridge, CB4 0WZ, United Kingdom. 

Registered Number: 3665875 | VAT number: GB787433096 

<table><tr><td>Revision</td><td>Date</td><td>Description</td></tr><tr><td>AA</td><td>March 2020</td><td>Initial release. Alternate document number: CS-00420281-PG</td></tr><tr><td>AB</td><td>May 2020</td><td>Updated for ADK 20.1.</td></tr><tr><td>AC</td><td>May 2020</td><td>Document title changed</td></tr><tr><td>AD</td><td>August 2020</td><td>Added Debug commands</td></tr><tr><td>AE</td><td>September 2020</td><td>Added ANC updates.</td></tr><tr><td>AF</td><td>December 2020</td><td>Updated for ADK 20.3</td></tr><tr><td>AG</td><td>March 2021</td><td>Added:
■ Get User Feature features
■ Audio Curation features
■ Fit Status features</td></tr><tr><td>AH</td><td>April 2022</td><td>Added:
■ Get Secondary Serial Number command
■ Secondary Connection Status notification
■ Handset service feature
■ Voice processing feature
■ UI Gesture configuration feature
■ Statistics reporting feature</td></tr></table>

Revision history 2 

1 GAIA v3 overview . 17 

1.1 Differences with earlier releases . 17 

1.2 Porting existing functionality to GAIA v3 . 17 

2 Protocol and packet reference . 19 

2.1 GAIA Wire protocol and packet format . 19 

2.1.1 RFCOMM and iAP 19 

2.1.2 LE GATT 20 

2.2 GAIA PDUs 20 

2.2.1 Command PDU 20 

2.2.2 Notification PDU 21 

2.2.3 Response PDU 21 

2.2.4 Error PDU 21 

2.2.5 Status codes 21 

2.3 Notifications . 22 

3 Command reference . 23 

3.1 GAIA framework feature . 23 

3.1.1 Get API Version command 23 

3.1.2 Get Supported Features command 24 

3.1.3 Get Supported Features Next command 24 

3.1.4 Get Serial Number command 25 

3.1.5 Get Variant command 25 

3.1.6 Get Application Version command . 26 

3.1.7 Device Reset command . 26 

3.1.8 Register Notification command 27 

3.1.9 Unregister Notification command 27 

3.1.10 Data Transfer Setup command 27 

3.1.11 Data Transfer Get command 28 

3.1.12 Data Transfer Set command 30 

3.1.13 Get Transport Information command 31 

3.1.14 Set Transport Parameter command 32 

3.1.15 Get User Feature command 33 

3.1.16 Get User Feature Next command 33 

3.1.17 Get Device Bluetooth Address command 34 

3.1.18 Charger Status notification 35 

# 3.2 Earbud Application feature . 35

3.2.1 Which Earbud is Primary command 36 

3.2.2 Get Secondary Serial Number command 36 

3.2.3 Primary Earbud About To Change notification 37 

3.2.4 Primary Earbud Changed notification 37 

3.2.5 Secondary Earbud Connection State notification . . 37 

# 3.3 Voice UI (VA) feature 38

3.3.1 Get Selected Assistant command . 38 

3.3.2 Set Selected Assistant command . . . 39 

3.3.3 Get Supported Assistants command 39 

3.3.4 Assistant Changed notification 39 

# 3.4 Debug commands 40

3.4.1 Get Debug Log Info command 40 

3.4.2 Configure Debug Logging command 41 

3.4.3 Get Panic Log command . . . . . . . 42 

3.4.4 Erase Panic Log command . . . . . 43 

3.4.5 Debug Tunnel To Chip command . 43 

3.4.6 GAIA Debug Error status code 44 

# 3.5 Music Processing feature . . 45

3.5.1 Get EQ State command 45 

3.5.2 Get Available EQ Pre-sets command 45 

3.5.3 Get EQ Set command 46 

3.5.4 Set EQ Set command . 46 

3.5.5 Get User Set Number Of Bands command 47 

3.5.6 Get User Set Configuration command 47 

3.5.7 Set User Set Configuration command 48 

3.5.8 EQ State Change notification 49 

3.5.9 EQ Set Change notification 49 

3.5.10 User EQ Band Change notification 49 

# 3.6 Device Firmware Upgrade (DFU) feature . 50

3.6.1 Upgrade Connect command 50 

3.6.2 Upgrade Disconnect command 50 

3.6.3 Upgrade Control command 51 

3.6.4 Get Data Endpoint Mode command 51 

3.6.5 Set Data Endpoint Mode command 52 

3.6.6 Upgrade Data Indication notification 52 

3.6.7 Upgrade Stop Request notification . 52 

3.6.8 Upgrade Start Request notification . 53 

# 3.7 Handset service feature . 53

3.7.1 Enable Multipoint command 53 

3.7.2 Multipoint Enabled Changed notification 54 

# 3.8 Audio Curation (AC) feature 54

3.8.1 Get State command 54 

3.8.2 Set State command 55 

3.8.3 Get Modes Count command 55 

3.8.4 Get Current Mode command 56 

3.8.5 Set Mode command 57 

3.8.6 Get Gain command 57 

3.8.7 Set Gain command 58 

3.8.8 Get Toggle Configuration Count command 58 

3.8.9 Get Toggle Configuration command 59 

3.8.10 Set Toggle Configuration command . . . 59 

3.8.11 Get Scenario Configuration command . 60 

3.8.12 Set Scenario Configuration command 61 

3.8.13 Get Demo Support command . 62 

3.8.14 Get Demo State command 62 

3.8.15 Set Demo State command 62 

3.8.16 Get Adaptation Control Status command 63 

3.8.17 Set Adaptation Control Status command 64 

3.8.18 Get Leakthrough dB Gain Slider Configuration command 64 

3.8.19 Get Current Leakthrough dB Gain Step command 65 

3.8.20 Set Leakthrough dB Gain Step command . 66 

3.8.21 Get Left Right Balance command 67 

3.8.22 Set Left Right Balance command 68 

3.8.23 Get Wind Noise Reduction Support command 68 

3.8.24 Get Wind Noise Detection State command 69 

3.8.25 Set Wind Noise Detection State command 69 

3.8.26 State Change notification 70 

3.8.27 Mode Change notification 70 

3.8.28 Gain Change notification 71 

3.8.29 Toggle Configuration notification 71 

3.8.30 Scenario Configuration notification . 72 

3.8.31 Demo State notification 72 

3.8.32 Adaptation Status Change notification 73 

3.8.33 Leakthrough dB Gain Slider Configuration notification 73 

3.8.34 Leakthrough dB Gain Change notification 74 

3.8.35 Left Right Balance notification 75 

3.8.36 Wind Noise Detection State change notification 75 

3.8.37 Wind Noise Reduction Indication notification . 75 

3.9 Fit Status feature . 76 

3.9.1 Set Fit Status command 76 

3.9.2 Fit Status Indication notification 76 

3.10 Voice Processing feature . 

3.10.1 Get Supported Enhancements command 

3.10.2 Set Config Enhancement command . 78 

3.10.3 Get Config Enhancement command . . . 79 

3.10.4 Notify Enhancement Mode Change notification 80 

3.11 UI Gesture Configuration feature . . . 80 

3.11.1 Get Number of Touchpads command . . 81 

3.11.2 Get Supported Gestures Command . . . . 82 

3.11.3 Get Supported Contexts command . . 82 

3.11.4 Get Supported Actions command . . . 83 

3.11.5 Get Configuration For Gesture command . 83 

3.11.6 Set Configuration For Gesture command 85 

3.11.7 Reset Configuration To Defaults command 87 

3.11.8 Gesture configuration Changed notification 87 

3.11.9 Configuration Reset To Defaults notification . 88 

3.12 Statistics reporting feature 88 

3.12.1 Get Supported Categories Command . 88 

3.12.2 Get All Statistics for Category command 89 

3.12.3 Get Statistics Values command 90 

Document references . 92 

Terms and definitions . 93 

# Tables

Table 2-1: RFCOMM and iAP protocol. 19 

Table 2-2: RFCOMM and iAP protocol with bit 1 of the flags set.. ........ 19 

Table 2-3: LE GATT protocol.. 20 

Table 2-4: GAIA v3 PDU protocol. 20 

Table 2-5: Default error codes.. 21 

Table 3-1: GAIA framework commands... .......... ..... 23 

Table 3-2: Get API Version command... 23 

Table 3-3: Get API Version Response PDU contents..... 23 

Table 3-4: Get Supported Features command.. . 24 

Table 3-5: Get Supported Features Response PDU contents... 24 

Table 3-6: Get Supported Features Next command............ .24 

Table 3-7: Get Supported Features Next Response PDU contents.. 24 

Table 3-8: Get Serial Number command........... 25 

Table 3-9: Get Serial Number Response PDU contents.. 25 

Table 3-10: Get Variant command. 25 

Table 3-11: Get Variant Response PDU contents.. 26 

Table 3-12: Get Application Version command. .26 

Table 3-13: Get Application Version Response PDU contents.. 26 

Table 3-14: Device Reset command. 26 

Table 3-15: Register Notification command. 27 

Table 3-16: Register Notification parameters.. 27 

Table 3-17: Unregister Notification command. 27 

Table 3-18: Unregister Notification command parameters.. 27 

Table 3-19: Data Transfer Setup command.. 28 

Table 3-20: Data Transfer Setup command parameters........... 28 

Table 3-21: Data Transfer Setup Response PDU contents............ 28 

Table 3-22: Data Transfer Setup command possible error status codes.. 28 

Table 3-23: Data Transfer Get command.... 29 

Table 3-24: Data Transfer Get command parameters................ 29 

Table 3-25: Data Transfer Get Response PDU contents.............. 29 

Table 3-26: Data Transfer Get command possible error status codes................... 29 

Table 3-27: Data Transfer Set command. 30 

Table 3-28: Data Transfer Set command parameters.. 30 

Table 3-29: Data Transfer Set Response PDU contents............ ...... 30 

Table 3-30: Data Transfer Set command possible error status codes. 30 

Table 3-31: Get Transport Information command. 31 

Table 3-32: Get Transport Information command parameters......... 31 

Table 3-33: Get Transport Information response PDU contents......... 31 

Table 3-34: Get Transport Information command possible error status codes.. .32 

Table 3-35: Set Transport Parameter command............ 32 

Table 3-36: Set Transport Parameter command parameters.. 32 

Table 3-37: Set Transport Parameter response PDU contents... 32 

Table 3-38: Set Transport Parameter command possible error status codes.. .33 

Table 3-39: Get User Feature command. 33 

Table 3-40: Get User Feature response PDU contents.. 33 

Table 3-41: Get User Feature command possible error status codes.. 33 

Table 3-42: Get User Feature Next command. 34 

Table 3-43: Get User Feature Next command parameters.. 34 

Table 3-44: Get User Feature Next command response PDU contents. 34 

Table 3-45: Get User Feature Next command possible error status codes..... 34 

Table 3-46: Get Device Bluetooth Address command. 34 

Table 3-47: Get Device Bluetooth Address Response PDU contents... 35 

Table 3-48: Charger Status notification.. . 35 

Table 3-49: Charger Status notification parameters.. 35 

Table 3-50: Earbud Application commands... 35 

Table 3-51: Which Earbud is Primary command... 36 

Table 3-52: Which Earbud is Primary command parameters.......... 36 

Table 3-53: Get Secondary Serial Number command... 36 

Table 3-54: Get Secondary Serial Number Response PDU parameters............. ..36 

Table 3-55: Primary Earbud About To Change notification..................... 37 

Table 3-56: Primary Earbud About to Change notification parameters........... 37 

Table 3-57: Primary Earbud Changed notification. 37 

Table 3-58: Primary Earbud Changed notification parameters............ 37 

Table 3-59: Secondary Earbud Connection State notification. 37 

Table 3-60: Primary Earbud Changed notification parameters.... ....... 38 

Table 3-61: VA commands.. 38 

Table 3-62: Get Selected Assistant command. 38 

Table 3-63: Get Selected Assistant Response PDU Contents........ 38 

Table 3-64: Assistant identifiers..... 38 

Table 3-65: Set Selected Assistant command. ........... 39 

Table 3-66: Set Selected Assistant parameters............. 39 

Table 3-67: Get Supported Assistants command............... 39 

Table 3-68: Get Supported Assistants Response PDU Contents.. .39 

Table 3-69: Assistant Changed notification. 39 

Table 3-70: Assistant Changed notification parameters....... 40 

Table 3-71: Debug commands.. 40 

Table 3-72: Get Debug Log Info command.. 40 

Table 3-73: Get Debug Log Info command parameters... 40 

Table 3-74: Debug Log Info Keys... 40 

Table 3-75: Get Debug Log Info Response PDU contents... 40 

Table 3-76: Get Debug Log Info command possible error status codes.... ........ 41 

Table 3-77: Get Debug Logging command.. 41 

Table 3-78: Configure Debug Logging command parameters.. 41 

Table 3-79: Configure Debug Logging Response PDU contents.... 42 

Table 3-80: Get Debug Log Info command possible error status codes.. 42 

Table 3-81: Get Panic Log command.. 42 

Table 3-82: Get Panic Log Response PDU contents... .42 

Table 3-83: Get Panic Log command possible error status codes.. 43 

Table 3-84: Erase Panic Log command.... 43 

Table 3-85: Erase Panic Log command parameters.. .43 

Table 3-86: Erase Panic Log command possible error status codes................ 43 

Table 3-87: Debug Tunnel To Chip command.. . 43 

Table 3-88: Debug Tunnel To Chip command parameters............. ................ 44 

Table 3-89: Debug Tunnel To Chip response PDU contents.. 44 

Table 3-90: Debug Tunnel To Chip command possible error status codes.. 44 

Table 3-91: GAIA Debug error status codes.. 44 

Table 3-92: Get EQ State command......................... ...................... 45 

Table 3-93: Get EQ State parameters............. 45 

Table 3-94: Get Available EQ Pre-sets command. 45 

Table 3-95: Get Available EQ response PDU contents......... .46 

Table 3-96: Get EQ Set command. 46 

Table 3-97: Get EQ Set response PDU contents............... 46 

Table 3-98: Set EQ Set command.. 46 

Table 3-99: Set EQ Set command parameters... 47 

Table 3-100: Set EQ Set response PDU contents. 47 

Table 3-101: Get User Set Number of Bands command.. 47 

Table 3-102: Get User Set Number of Bands response PDU contents. 47 

Table 3-103: Get User Set Configuration command. 47 

Table 3-104: Get User Set Configuration command parameters.. 48 

Table 3-105: Get User Set Configuration Response PDU contents.... 48 

Table 3-106: Set User Set Configuration command.. 48 

Table 3-107: Set User Set command parameters........... 48 

Table 3-108: Set User Set Response PDU contents. 49 

Table 3-109: EQ Change notification................ 49 

Table 3-110: EQ State Change notification parameters.. 49 

Table 3-111: EQ Set Change notification................ 49 

Table 3-112: EQ Set Change notification parameters. 49 

Table 3-113: User EQ Band Change notification............... 49 

Table 3-114: User EQ Band Change notification parameters........ 50 

Table 3-115: DFU commands... 50 

Table 3-116: Upgrade Connect command.. 50 

Table 3-117: Upgrade Disconnect command....................... 50 

Table 3-118: Upgrade Control command.. 51 

Table 3-119: Get Data Endpoint Mode command.. 51 

Table 3-120: Get Data Endpoint Mode Response PDU parameters................ 51 

Table 3-121: Set Data Endpoint Mode command.. 52 

Table 3-122: Set Data Endpoint Mode command parameters..... .......... 52 

Table 3-123: Upgrade Data Indication notification. 52 

Table 3-124: Upgrade Data Indication notification parameters... 52 

Table 3-125: Upgrade Stop Request notification...... 52 

Table 3-126: Upgrade Stop Request notification parameters............... 53 

Table 3-127: Upgrade Start Request notification........................ 53 

Table 3-128: Upgrade Start Request notification parameters.. 53 

Table 3-129: Enable Multipoint command....................... 53 

Table 3-130: Enable Multipoint command parameters. 53 

Table 3-131: Multipoint Enabled Changed notification. 54 

Table 3-132: Multipoint Enabled Changed notification parameters............. 54 

Table 3-133: AC commands.. 54 

Table 3-134: Get State command. 54 

Table 3-135: Get State command parameters... 54 

Table 3-136: Get State response parameters... 54 

Table 3-137: Set State command. 55 

Table 3-138: Set State command parameters.......... 55 

Table 3-139: Get Modes Count command.. 55 

Table 3-140: Get Num Modes response parameters.. 56 

Table 3-141: Get Current ANC Mode command. 56 

Table 3-142: Get Current ANC Mode response parameters.. 56 

Table 3-143: Set Mode command. 57 

Table 3-144: Set Mode command parameters.. .57 

Table 3-145: Get Gain command. 57 

Table 3-146: Get Gain response PDU contents........... 57 

Table 3-147: Get Gain possible Error status code......................... 58 

Table 3-148: Set Gain command.. 58 

Table 3-149: Set Gain command parameters.... . 58 

Table 3-150: Set Gain possible Error status code................... . 58 

Table 3-151: Get Toggle Configuration Count command.. 58 

Table 3-152: Get Toggle Configuration Count response PDU contents... ： .59 

Table 3-153: Get Toggle Configuration command. .59 

Table 3-154: Get Toggle Configuration command parameters. 59 

Table 3-155: Get Toggle Configuration response PDU contents........ 59 

Table 3-156: Get Toggle Configuration possible Error status code...... 59 

Table 3-157: Set Toggle Configuration command.. 59 

Table 3-158: Set Toggle Configuration command parameters...... 60 

Table 3-159: Set Toggle Configuration possible Error status code. 60 

Table 3-160: Get Scenario Configuration command............... 60 

Table 3-161: Get Scenario Configuration command parameters.. . 60 

Table 3-162: Get Scenario Configuration response PDU contents. . 60 

Table 3-163: Get Scenario Configuration possible Error status code. 61 

Table 3-164: Set Scenario Configuration command.. . 61 

Table 3-165: Set Scenario Configuration command parameters.. .61 

Table 3-166: Set Scenario Configuration possible Error status code. 62 

Table 3-167: Get Demo Support command. 62 

Table 3-168: Get Demo Support response PDU contents... 62 

Table 3-169: Get Demo State command. 62 

Table 3-170: Get Demo State command response PDU contents.. . 62 

Table 3-171: Set Demo State command. . 63 

Table 3-172: Set Demo Support command parameters.. .63 

Table 3-173: Set Demo State command possible error status codes. . 63 

Table 3-174: Get Adaptation Control Status command. 63 

Table 3-175: Get Adaptation Control Status response PDU contents.. 63 

Table 3-176: Get Adaptation Control Status possible error status codes.. 63 

Table 3-177: Set Adaptation Control Status command.. 64 

Table 3-178: Set Adaptation Control Status command parameters.. .64 

Table 3-179: Set Adaptation Control Status possible error status codes. 64 

Table 3-180: Get Leakthrough dB Gain Slider Configuration command... 64 

Table 3-181: Get Leakthrough dB Gain Slider Configuration command response PDU contents........................... 64 

Table 3-182: Get Leakthrough dB Gain Slider Configuration command possible feature specific error status codes... ...... 65 

Table 3-183: Get Current Leakthrough dB Gain Step command......... 65 

Table 3-184: Get Current Leakthrough dB Gain Step command response PDU contents. .66 

Table 3-185: Get Current Leakthrough dB Gain Step command response PDU contents. .66 

Table 3-186: Set Leakthrough dB Gain Step command.. .66 

Table 3-187: Set Leakthrough dB Gain Step command parameters..................... 66 

Table 3-188: Set Leakthrough dB Gain Step command possible feature specific error status codes.. .67 

Table 3-189: Get Left Right Balance command.... 67 

Table 3-190: Get Left Right Balance command response PDU contents.. .67 

Table 3-191: Get Left Right Balance command possible feature specific error status codes. . 67 

Table 3-192: Set Left Right Balance command......... . 68 

Table 3-193: Set Left Right Balance command parameters.............. 68 

Table 3-194: Set Left Right Balance command possible feature specific error status codes................ 68 

Table 3-195: Get Wind Noise Reduction Support command.. 68 

Table 3-196: Get Wind Noise Reduction Support command response PDU contents..................... 69 

Table 3-197: Get Wind Noise Detection State command. 69 

Table 3-198: Get Wind Noise Detection State command response PDU contents... 69 

Table 3-199: Set Wind Noise Detection State command.. 69 

Table 3-200: Set Wind Noise Detection State command parameters... . 69 

Table 3-201: State Change notification.. 70 

Table 3-202: State Change notification parameters................... 70 

Table 3-203: Mode Change notification.. .70 

Table 3-204: Mode Change notification parameters. 70 

Table 3-205: Gain Change notification. 71 

Table 3-206: Gain Change notification parameters.. 71 

Table 3-207: Toggle Configuration notification. 71 

Table 3-208: Toggle Configuration notification parameters... .72 

Table 3-209: Scenario Configuration notification.. 72 

Table 3-210: Scenario Configuration notification parameters... 72 

Table 3-211: Demo State notification. 72 

Table 3-212: Demo State notification parameters.. 73 

Table 3-213: Adaptation Status Change notification.. 73 

Table 3-214: Adaptation Status Change notification parameters.. 73 

Table 3-215: Leakthrough dB Gain Slider Configuration notification. .73 

Table 3-216: Leakthrough dB Gain Slider Configuration notification parameters............. .73 

Table 3-217: Leakthrough dB Gain Change notification.................. ............. .74 

Table 3-218: Leakthrough dB Gain Change notification.............. 74 

Table 3-219: Left Right Balance notification........... 75 

Table 3-220: Left Right Balance notification parameters.... 75 

Table 3-221: Wind Noise Detection State change notification. 75 

Table 3-222: Wind Noise Detection State change notification parameters. 75 

Table 3-223: Wind Noise Reduction Indication notification... .75 

Table 3-224: Wind Noise Reduction Indication notification parameters. 76 

Table 3-225: Fit Status... 76 

Table 3-226: Set Fit Status command. 76 

Table 3-227: Set Mode command parameters... . 76 

Table 3-228: Fit Status Indication notification.. 76 

Table 3-229: Fit Status Indication notification parameters.. 77 

Table 3-230: Get Supported Enhancements command. 77 

Table 3-231: Get Supported Enhancements command parameters.. 77 

Table 3-232: Get Supported Enhancements command response PDU contents............... . 78 

Table 3-233: Set Config Enhancement command. 78 

Table 3-234: Set Config Enhancement command parameters................ . 78 

Table 3-235: Set Config Enhancement command possible feature error status codes.. 78 

Table 3-236: Get Config Enhancement command. 79 

Table 3-237: Get Config Enhancement command parameters.. 79 

Table 3-238: GetConfigEnhancement command response PDU contents for capability CVC_3MIC....................... 79 

Table 3-239: Get Config Enhancement command possible feature specific error status codes.. 79 

Table 3-240: Notify Enhancement Mode Change notification.................. 80 

Table 3-241: Notify Enhancement Mode Change notification parameters... .80 

Table 3-242: UI Gesture Configuration.............. 81 

Table 3-243: Get Number of Touchpads command.. 81 

Table 3-244: Get Number of Touchpads command response PDU contents. 81 

Table 3-245: Get Supported Gestures command. .... 82 

Table 3-246: Get Supported Gestures command response PDU contents............ 82 

Table 3-247: Get Supported Contexts command.. 82 

Table 3-248: Get Supported Contexts command response PDU contents......... 83 

Table 3-249: Get Supported Actions command. 83 

Table 3-250: Get Supported Actions command response PDU contents.. .83 

Table 3-251: Get Configuration For Gesture command.. 84 

Table 3-252: Get Configuration For Gesture command parameters.................. 84 

Table 3-253: Get Configuration For Gesture command parameters.. . 84 

Table 3-254: Get Configuration For Gesture command response PDU contents.. . 84 

Table 3-255: Set Configuration For Gesture command. . 85 

Table 3-256: Set Configuration For Gesture command parameters.. .86 

Table 3-257: Reset Configuration To Defaults command.. 87 

Table 3-258: Reset Configuration To Defaults command possible feature specific error status codes. .87 

Table 3-259: Gesture configuration Changed notification... 87 

Table 3-260: Gesture configuration Changed notification parameters.. . 88 

Table 3-261: Configuration Reset to Defaults notification.. 88 

Table 3-262: Statistics reporting feature. . 88 

Table 3-263: Get Supported Categories command.............. 88 

Table 3-264: Get Supported Categories command parameters. .89 

Table 3-265: Get Supported Categories command response PDU contents............... 89 

Table 3-266: Get All Statistics For Category command.. 89 

Table 3-267: Get All Statistics For Category command parameters............. 89 

Table 3-268: Get All Statistics For Category command response PDU contents.. . 90 

Table 3-269: Get Statistics Values command. 90 

Table 3-270: Get Statistics Values command parameters... 90 

Table 3-271: Get Statistics Values command response PDU contents.. 90 

Table 3-272: Statistic value entry........... 91 

# 1 GAIA v3 overview

Qualcomm Generic Application Interface Architecture (GAIA) implements an end-to-end, hostagnostic ecosystem supporting host application access to device functionality. This document describes the wire protocol/packet structure of QTIL GAIA and the QTIL GAIA command packet structure and commands. It also gives guidance on porting existing functionality to GAIA v3. 

# 1.1 Differences with earlier releases

■ New GAIA Framework domain component 

■ GAIA Library APIs deprecated 

■ GAIA over iAP is not supported in this release 

■ New Vendor ID for QTIL commands 

New packet format of QTIL commands 

GAIA version number is now 3 

■ Audio Curation Feature ID is now 8 

# 1.2 Porting existing functionality to GAIA v3

This section describes how to port existing GAIA commands to use the GAIA Framework component and the new QTIL command packet format. 

# Mobile applications

If you are configuring your mobile application needs to support QTIL commands, ensure that you support the following components: 

Support the new QTIL Vendor ID (0x001D) 

■ Support the new GAIA Version number (3) 

■ Support the new QTIL command packet structure and protocol 

You also need to decide whether you need to support both old and new versions of GAIA. 

# Devices

Porting existing functionality to GAIA v3 involves: 

■ Porting functionality to the ADK20.1 application 

■ Porting existing commands to GAIA v3 

To add your commands to GAIA v3, use the GAIA Framework API GaiaFramework_RegisterVendorSpecificHandler(). 

This API registers a callback function that is called when the GAIA Framework receives a Vendor ID that is not the QTIL Vendor ID. This function is passed a GAIA_UNHANDLED_COMMAND_IND message. 

The GAIA Framework then provides APIs that send responses back to the mobile app. 

# 2.1 GAIA Wire protocol and packet format

# 2.1.1 RFCOMM and iAP


Table 2-1 RFCOMM and iAP protocol


<table><tr><td>0</td><td>1</td><td>2</td><td>3</td><td>4+</td><td>Length + 1</td></tr><tr><td>SOF</td><td>Version</td><td>Flags</td><td>Length</td><td>PDU</td><td>CS</td></tr></table>

Where: 

SOF is Start of Frame: 0xFF 

Version: 3 or greater 

■ Flags: 

□ Bit 0 set: Checksum in use 

□ Bit 1 set: Length extension in use, from wire protocol version 4 


Table 2-2 RFCOMM and iAP protocol with bit 1 of the flags set


<table><tr><td>0</td><td>1</td><td>2</td><td>3</td><td>4</td><td>5+</td><td>Length + 1</td></tr><tr><td>SOF</td><td>Version</td><td>Flags</td><td colspan="2">Length</td><td>PDU</td><td>CS</td></tr></table>

□ Bits 1 to 7 are reserved 

■ Length of Payload: 

□ Over 1 byte: Maximum length is 254 bytes 

□ Over 2 bytes: the 16-bit length field is big-endian encoded, maximum length is 65634 bytes 

■ Protocol Data Unit (PDU): See GAIA PDUs. 

CS: Simple XOR of all bytes in the packet. The field is not included when the Bit 0 of the flags is set to 0. 

# 2.1.2 LE GATT

If LE DLE is not present (for example, in models older than iPhone 7), LE GATT packets are much shorter. Therefore LE GATT protocol is simpler to reduce the overhead. 


Table 2-3 LE GATT protocol


<table><tr><td>0+</td></tr><tr><td>PDU</td></tr></table>

Where: 

■ PDU: See Gaia PDUs. 

# 2.2 GAIA PDUs


Table 2-4 GAIA v3 PDU protocol


<table><tr><td>Byte 0</td><td>Byte 1</td><td colspan="8">Byte 2</td><td colspan="8">Byte 3</td><td>Bytes 4+</td></tr><tr><td>-</td><td>-</td><td>7</td><td>6</td><td>5</td><td>4</td><td>3</td><td>2</td><td>1</td><td>0</td><td>7</td><td>6</td><td>5</td><td>4</td><td>3</td><td>2</td><td>1</td><td>0</td><td>-</td></tr><tr><td colspan="2">Vendor ID</td><td colspan="7">Feature ID</td><td>PDU Type</td><td colspan="8">PDU Specific ID</td><td>Payload</td></tr></table>

Where: 

Vendor ID: Unique, vendor-specific identifier. For example, Bluetooth SIG already has assigned numbers identifying companies. 

■ Feature ID: Feature-specific unique identifier. 

■ PDU Type: 

□ Command: 00 

□ Notification: 01 

□ Response: 10 

□ Error: 11 

■ PDU Specific ID: Feature-specific command ID. These are only unique within the Feature. 

■ Payload: Optional payload. 

# 2.2.1 Command PDU

Direction: Mobile application to the device. 

These PDUs are sent by the mobile application to get the device to do something. The PDU-specific ID is called a Command ID. 

# 2.2.2 Notification PDU

Direction: Device to the mobile application. 

These are sent by the device to the mobile application generally with status changes. The PDUspecific ID is called a Notification ID. 

# 2.2.3 Response PDU

Direction: Device to mobile application. 

This PDU is sent in response to a Command being successfully processed. It may contain a command-specific payload. 

# 2.2.4 Error PDU

Direction: Device to mobile application 

This PDU is sent in response to a Command being unsuccessfully processed. The PDU payload contains a status code. 


Table 2-5 Default error codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Failed Feature Not Supported</td><td>0x00</td><td>An invalid Feature ID was specified.</td></tr><tr><td>Failed Command Not Supported</td><td>0x01</td><td>An invalid PDU Specific ID was specified.</td></tr><tr><td>Failed Not Authenticated</td><td>0x02</td><td>The host is not authenticated to use a Command ID or control.</td></tr><tr><td>Failed Insufficient Resources</td><td>0x03</td><td>The command was valid, but the device could not successfully carry out the command.</td></tr><tr><td>Authenticating</td><td>0x04</td><td>The device is in the process of authenticating the host.</td></tr><tr><td>Invalid Parameter</td><td>0x05</td><td>An invalid parameter was used in the command.</td></tr><tr><td>Incorrect State</td><td>0x06</td><td>The device is not in the correct state to process the command.</td></tr><tr><td>In Progress</td><td>0x07</td><td>The command is in progress.</td></tr></table>

# 2.2.5 Status codes

Status codes are 8 bits long where: 

■ Values 0 to 127 are GAIA Framework values 

■ Values 128 to 256 are Feature-specific values 

# 2.3 Notifications

Notifications are a GAIA PDU type that enable the device to send information to the mobile application when necessary. To reduce the load on Bluetooth airtime, Feature Notifications are only sent when the mobile application registers to receive them. 

The following two commands control Notifications: 

■ ■ Register 

Unregister 

# 3.1 GAIA framework feature


Table 3-1 GAIA framework commands


<table><tr><td>Feature ID</td><td>Version</td><td>ADK release</td></tr><tr><td rowspan="4">0x00</td><td>1</td><td>20.1</td></tr><tr><td>2</td><td>20.2</td></tr><tr><td>3</td><td>21.1</td></tr><tr><td>4</td><td>21.1</td></tr></table>

# 3.1.1 Get API Version command

The Get API Version command gets the GAIA protocol version number, which is 3 or greater. 


Table 3-2 Get API Version command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1+</td><td>20.1</td><td>All</td></tr></table>

Command parameters 

None 


Table 3-3 Get API Version Response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>≥ 3</td><td>GAIA version major</td></tr><tr><td>2</td><td></td><td>GAIA version minor</td></tr></table>

Possible Feature-specific Error Status codes 

None 

# 3.1.2 Get Supported Features command

The Get Supported Features command gets the list of features the device supports. If the list is too long to fit into a single packet, then the More data field is set, and the Get Supported Features Next command should be used to retrieve the remainder of the list. 


Table 3-4 Get Supported Features command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>1</td><td>1+</td><td>20.1</td><td>All</td></tr></table>

# Command parameters

None 

Where the Feature ID Is bit is aligned into the response packet Feature ID ls bit. 

If no features are supported, then the Response PDU payload is empty. 


Table 3-5 Get Supported Features Response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>1</td><td>More data</td></tr><tr><td>1</td><td>0</td><td>No more data</td></tr><tr><td>2</td><td>-</td><td>1st Feature ID</td></tr><tr><td>3</td><td>-</td><td>1st Feature Version</td></tr><tr><td>4</td><td>-</td><td>2nd Feature ID</td></tr><tr><td>5</td><td>-</td><td>2nd Feature Version</td></tr><tr><td>6+</td><td>-</td><td>List continues</td></tr></table>

# Possible Feature-specific error status codes

None 

# 3.1.3 Get Supported Features Next command

The Get Supported Features Next command gets the list continuation of features the device supports. 


Table 3-6 Get Supported Features Next command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>2</td><td>2+</td><td>20.2</td><td>All</td></tr></table>


Table 3-7 Get Supported Features Next Response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>1</td><td>More data</td></tr><tr><td>1</td><td>0</td><td>No more data</td></tr><tr><td>2</td><td>-</td><td>1st Feature ID</td></tr><tr><td>3</td><td>-</td><td>1st Feature Version</td></tr><tr><td>4</td><td>-</td><td>2nd Feature ID</td></tr><tr><td>5</td><td>-</td><td>2nd Feature Version</td></tr><tr><td>6+</td><td>-</td><td>List continues</td></tr></table>

Where the Feature ID ls bit is aligned into the response packet Feature ID Is bit. 

If no features are supported, then the Response PDU payload is empty. 

Possible feature-specific Error Status codes 

None 

# 3.1.4 Get Serial Number command

The Get Serial Number command gets the customer-provided serial number for this device. 


Table 3-8 Get Serial Number command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>3</td><td>1+</td><td>20.1</td><td>All</td></tr></table>

# Command parameters

None 

If no serial number is set, then the Response PDU payload is empty. 


Table 3-9 Get Serial Number Response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1+</td><td>-</td><td>Serial number text</td></tr></table>

Possible Feature specific Error Status codes 

None 

# 3.1.5 Get Variant command

The Get Variant command gets the customer-provided variant name. 


Table 3-10 Get Variant command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>4</td><td>1+</td><td>20.1</td><td>All</td></tr></table>

# Command parameters

None 

If no variant name is set, then the Response PDU payload is empty. 


Table 3-11 Get Variant Response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1+</td><td>-</td><td>Variant text</td></tr></table>

Possible Feature-specific Error Status codes 

None 

# 3.1.6 Get Application Version command

The Get Application Version command gets the customer-provided application version number. 


Table 3-12 Get Application Version command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>5</td><td>1+</td><td>20.1</td><td>All</td></tr></table>

Command parameters 

None 

If no application version is set, then the Response PDU payload is empty. 


Table 3-13 Get Application Version Response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1+</td><td>-</td><td>Application version text</td></tr></table>

Possible Feature specific Error Status codes 

None 

# 3.1.7 Device Reset command

The Device Reset command enables the mobile app to cause a device to warm reset. The device transmits the response, and then does a warm reset. 


Table 3-14 Device Reset command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>6</td><td>1+</td><td>20.1</td><td>All</td></tr></table>

Command parameters 

None 

Response PDU contents 

None 

Possible feature-specific error status codes 

None 

# 3.1.8 Register Notification command

The Register Notification command enables the mobile application to register to receive all notifications from a feature. 


Table 3-15 Register Notification command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>7</td><td>1+</td><td>20.1</td><td>All</td></tr></table>


Table 3-16 Register Notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>-</td><td>Feature ID</td></tr></table>

Response PDU contents 

None 

Possible Feature specific Error Status codes 

None 

# 3.1.9 Unregister Notification command

The Unregister Notification command enables the mobile application to unregister to stop receiving feature notifications. 


Table 3-17 Unregister Notification command


<table><tr><td>Command ID</td><td>ADK release</td><td>Variants</td></tr><tr><td>8</td><td>20.1</td><td>All</td></tr></table>


Table 3-18 Unregister Notification command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>-</td><td>Feature ID</td></tr></table>

Response PDU contents 

None 

Possible feature-specific error status codes 

None 

# 3.1.10 Data Transfer Setup command

The Data Transfer Setup command can be used by the mobile application to set up a data transfer channel. 

Before using this command, the mobile application needs to send a GAIA Feature command that opens a data transfer session with a Session ID. This 16-bit Session ID and a Transport Type must be specified as Data Transfer Setup command parameters. 

1. The device checks if both the command parameters (Session ID, Transport Type) are valid. 

2. It then finds the feature tied to the Session ID. 

3. The device then opens a data transfer channel and binds the channel with the feature’s data transfer handler. 


Table 3-19 Data Transfer Setup command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>9</td><td>2</td><td>20.2</td><td>All</td></tr></table>


Table 3-20 Data Transfer Setup command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>(any)</td><td>Session ID (MSB)</td></tr><tr><td>2</td><td>(any)</td><td>Session ID (LSB)</td></tr><tr><td>3</td><td>1</td><td>Transport Type:
■ 0: None (Invalid value as the parameter)
■ 1: GAIA_COMMAND Link</td></tr></table>


Table 3-21 Data Transfer Setup Response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>(any)</td><td>Session ID (MSB)</td></tr><tr><td>2</td><td>(any)</td><td>Session ID (LSB)</td></tr></table>


Table 3-22 Data Transfer Setup command possible error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Failed Insufficient Resources</td><td>0x03</td><td>The command was valid, but the device could not successfully carry out the command.</td></tr><tr><td>Invalid Parameter</td><td>0x05</td><td>An invalid parameter was used in the command.</td></tr><tr><td>Incorrect State</td><td>0x06</td><td>The device is not in the correct state to process the command.</td></tr></table>

# 3.1.11 Data Transfer Get command

The Data Transfer Get command can be used by the mobile application to request the device to send data bytes as the response. 

Before using this command, the mobile application must set up a data transfer channel to the device with the Data Transfer Setup command. 


Table 3-23 Data Transfer Get command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>10</td><td>2</td><td>20.2</td><td>All</td></tr></table>


Table 3-24 Data Transfer Get command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>(any)</td><td>Session ID (MSB)</td></tr><tr><td>2</td><td>(any)</td><td>Session ID (LSB)</td></tr><tr><td>3</td><td>(any)</td><td>Start offset (MSB)</td></tr><tr><td>4</td><td>(any)</td><td>Start offset (Second SB)</td></tr><tr><td>5</td><td>(any)</td><td>Start offset (Third SB)</td></tr><tr><td>6</td><td>(any)</td><td>Start offset (LSB)</td></tr><tr><td>7</td><td>(any)</td><td>Data size (MSB)</td></tr><tr><td>8</td><td>(any)</td><td>Data size (Second SB)</td></tr><tr><td>9</td><td>(any)</td><td>Data size (Third SB)</td></tr><tr><td>10</td><td>(any)</td><td>Data size (LSB)</td></tr></table>


Table 3-25 Data Transfer Get Response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>(any)</td><td>Session ID (MSB)</td></tr><tr><td>2</td><td>(any)</td><td>Session ID (LSB)</td></tr><tr><td>3</td><td>(any)</td><td>Data (Offset + 0)</td></tr><tr><td>4</td><td>(any)</td><td>Data (Offset + 1)</td></tr><tr><td>5</td><td>(any)</td><td>Data (Offset + 2)</td></tr><tr><td>...</td><td>...</td><td>...</td></tr><tr><td>N + 3</td><td>(any)</td><td>Data (Offset + N)</td></tr></table>


Table 3-26 Data Transfer Get command possible error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Failed Insufficient Resources</td><td>0x03</td><td>The command was valid, but the device could not successfully carry out the command.</td></tr><tr><td>Invalid Parameter</td><td>0x05</td><td>An invalid parameter was used in the command.</td></tr><tr><td>Incorrect State</td><td>0x06</td><td>The device is not in the correct state to process the command.</td></tr></table>


NOTE GAIA Debug Feature uses Data Transfer Get command to read panic log from the device. 


# 3.1.12 Data Transfer Set command

The Data Transfer Set command can be used by the mobile application to send data bytes to the device. 

Before using this command, the mobile application must set up a data transfer channel to the device with the Data Transfer Setup command. 


Table 3-27 Data Transfer Set command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>11</td><td>2</td><td>20.2</td><td>All</td></tr></table>


Table 3-28 Data Transfer Set command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>(any)</td><td>Session ID (MSB)</td></tr><tr><td>2</td><td>(any)</td><td>Session ID (LSB)</td></tr><tr><td>3</td><td>(any)</td><td>Start offset (MSB)</td></tr><tr><td>4</td><td>(any)</td><td>Start offset (Second SB)</td></tr><tr><td>5</td><td>(any)</td><td>Start offset (Third SB)</td></tr><tr><td>6</td><td>(any)</td><td>Start offset (LSB)</td></tr><tr><td>7</td><td>(any)</td><td>Data (Offset + 0)</td></tr><tr><td>8</td><td>(any)</td><td>Data (Offset + 1)</td></tr><tr><td>9</td><td>(any)</td><td>Data (Offset + 2)</td></tr><tr><td>...</td><td>...</td><td>...</td></tr><tr><td>N + 7</td><td>(any)</td><td>Data (Offset + N)</td></tr></table>


Table 3-29 Data Transfer Set Response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>(any)</td><td>Session ID (MSB)</td></tr><tr><td>2</td><td>(any)</td><td>Session ID (LSB)</td></tr></table>


Table 3-30 Data Transfer Set command possible error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Failed Insufficient Resources</td><td>0x03</td><td>The command was valid, but the device could not successfully carry out the command.</td></tr><tr><td>Invalid Parameter</td><td>0x05</td><td>An invalid parameter was used in the command.</td></tr><tr><td>Incorrect State</td><td>0x06</td><td>The device is not in the correct state to process the command.</td></tr></table>

# 3.1.13 Get Transport Information command

The Get Transport Information command can be used by the mobile application to get information about the GAIA transport. 


Table 3-31 Get Transport Information command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>12</td><td>2</td><td>20.2</td><td>All</td></tr></table>


Table 3-32 Get Transport Information command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>-</td><td>Transport key: 
■ 1: Maximum packet size in bytes the device can send 
■ 2: Optimum packet size in bytes the device should send. Optimum is generally taken to mean fastest transfer. 
■ 3: Maximum packet size in bytes the device can receive. 
■ 4: Optimum packet size in bytes the device should receive. Optimum is generally taken to mean fastest transfer. 
■ 5: Transport flow control in device to phone direction. 
□ 1: Flow control 
□ 0: No flow control 
■ 6: Transport flow control in phone to device direction. 
□ 1: Flow control 
□ 0: No flow control 
■ 7: Current protocol version</td></tr></table>

NOTE For RFCOMM and iAP2, PACKET_SIZES are the size of the GAIA packet and do not include any transport or lower protocol headers. For Bluetooth Low Energy/GATT, the PACKET_SIZES include the 3 byte ATT header, so a reported size of 65 actually means a GAIA packet size of 62. 


Table 3-33 Get Transport Information response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>-</td><td>Transport key from command</td></tr><tr><td>2</td><td>(any)</td><td rowspan="4">Value of requested key as unsigned 32-bit value, in big endian format</td></tr><tr><td>3</td><td>(any)</td></tr><tr><td>4</td><td>(any)</td></tr><tr><td>5</td><td>(any)</td></tr></table>


Table 3-34 Get Transport Information command possible error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Failed Insufficient Resources</td><td>0x03</td><td>The command was valid, but the device could not successfully carry out the command.</td></tr><tr><td>Invalid Parameter</td><td>0x05</td><td>An invalid parameter was used in the command.</td></tr></table>

# 3.1.14 Set Transport Parameter command

The Set Transport Parameter command can be used by the mobile application to request changing a GAIA transport parameter. If the parameter value is accepted it will be returned in the response, if the value is not accepted the old value or a value that is as close as possible to the requested value will be returned. 


Table 3-35 Set Transport Parameter command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>13</td><td>2</td><td>20.2</td><td>All</td></tr></table>


Table 3-36 Set Transport Parameter command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>-</td><td>Transport key: 
■ 1: Maximum packet size in bytes the device can send 
■ 2: Optimum packet size in bytes the device should send. Optimum is generally taken to mean fastest transfer. 
■ 5: TX_FLOW_CONTROL 
■ 7: Protocol version</td></tr><tr><td>2</td><td>(any)</td><td rowspan="4">Requested value of key as unsigned 32-bit value, in big endian format</td></tr><tr><td>3</td><td>(any)</td></tr><tr><td>4</td><td>(any)</td></tr><tr><td>5</td><td>(any)</td></tr></table>


Table 3-37 Set Transport Parameter response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>-</td><td>Transport key from command</td></tr><tr><td>2</td><td>(any)</td><td rowspan="4">Actual value of key as unsigned 32-bit value, in big endian format</td></tr><tr><td>3</td><td>(any)</td></tr><tr><td>4</td><td>(any)</td></tr><tr><td>5</td><td>(any)</td></tr></table>


Table 3-38 Set Transport Parameter command possible error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Failed Insufficient Resources</td><td>0x03</td><td>The command was valid, but the device could not successfully carry out the command.</td></tr><tr><td>Invalid Parameter</td><td>0x05</td><td>An invalid parameter was used in the command.</td></tr></table>

# 3.1.15 Get User Feature command

The Get User Feature command can be used by the mobile application to read the user feature data from the device. If the data is available, the data starting from the first byte is set to the response payload. If the whole data does not fit into the response payload, the MoreData bit in the response is set to 1. The remaining data can be read with the Get User Feature Next command. 


Table 3-39 Get User Feature command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>14</td><td>3</td><td>21.1</td><td>All</td></tr></table>

Command parameters 

None. 


Table 3-40 Get User Feature response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x00</td><td>■ Bits [7:1]: Reserved for future use.</td></tr><tr><td></td><td>or</td><td>☐ Bit [0]: MoreData. If this bit is set to 1, there is remaining data that did not fit in this response payload.</td></tr><tr><td></td><td>0x01</td><td></td></tr><tr><td>2</td><td>(any)</td><td>Reading Status: This three-byte status parameter is used as the parameter for the Get User Feature Next command to read the remaining data.</td></tr><tr><td>3</td><td></td><td></td></tr><tr><td>4</td><td></td><td></td></tr><tr><td>5</td><td>(any)</td><td>Data bytes of the user feature data (if any).</td></tr></table>


Table 3-41 Get User Feature command possible error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Invalid parameter</td><td>0x05</td><td>An invalid parameter was used in the command.</td></tr></table>

# 3.1.16 Get User Feature Next command

The Get User Feature Next command can be used by the mobile application to read the continuous part of the user feature data, which has not been read by the last Get User Feature (Next) command. 

If the data is available, the continuous data is set to the response payload. If the remaining data does not fit into the response payload yet, the MoreData bit in the response is set to 1. Another Get User 

Feature Next command or commands can be used to read the following data until the mobile application receives the response with the MoreData bit cleared to zero. 


Table 3-42 Get User Feature Next command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>15</td><td>3</td><td>21.1</td><td>All</td></tr></table>


Table 3-43 Get User Feature Next command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td rowspan="3">(any)</td><td rowspan="3">Reading Status: The three-byte status parameter that has been returned from the device as a part of the response of the preceding Get User Feature (Next) command.</td></tr><tr><td>2</td></tr><tr><td>3</td></tr></table>


Table 3-44 Get User Feature Next command response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="3">1</td><td>0x00</td><td>Bits [7:1]: Reserved for future use.</td></tr><tr><td>or</td><td rowspan="2">Bit [0]: MoreData. If this bit is set to 1. there is remaining data that did not fit in this response payload.</td></tr><tr><td>0x01</td></tr><tr><td>2</td><td rowspan="3">(any)</td><td rowspan="3">Reading Status: This three-byte status parameter is used as the parameter for the Get User Feature Next command to read the remaining data.</td></tr><tr><td>3</td></tr><tr><td>4</td></tr><tr><td>5</td><td>(any)</td><td>Data bytes of the user feature data (if any).</td></tr></table>


Table 3-45 Get User Feature Next command possible error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Invalid parameter</td><td>0x05</td><td>An invalid parameter was used in the command.</td></tr></table>

# 3.1.17 Get Device Bluetooth Address command

The Get Device Bluetooth Address command gets the permanent (BR/EDR) Bluetooth Device address. For a pair of earbuds this is the address of the primary earbud. 


Table 3-46 Get Device Bluetooth Address command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>16</td><td>4</td><td></td><td>All</td></tr></table>

Command parameters 

None. 


Table 3-47 Get Device Bluetooth Address Response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>-</td><td>NAP, MSB</td></tr><tr><td>2</td><td>-</td><td>NAP, LSB</td></tr><tr><td>3</td><td>-</td><td>UAP</td></tr><tr><td>4</td><td>-</td><td>LAP, MSB</td></tr><tr><td>5</td><td>-</td><td>LAP</td></tr><tr><td>6</td><td>-</td><td>LAP, LSB</td></tr></table>

Possible Feature specific Error Status codes 

None. 

# 3.1.18 Charger Status notification

The Charger Status notification enables the device to generate a notification when the charger is plugged in or unplugged. 


Table 3-48 Charger Status notification


<table><tr><td>Notification ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1</td><td>20.1</td><td>All</td></tr></table>


Table 3-49 Charger Status notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td>0x00</td><td>Unplugged</td></tr><tr><td>0x01</td><td>Plugged in</td></tr></table>

# 3.2 Earbud Application feature


Table 3-50 Earbud Application commands


<table><tr><td>Feature ID</td><td>Version</td><td>ADK release</td></tr><tr><td rowspan="3">0x01</td><td>1</td><td>20.1</td></tr><tr><td>1</td><td>20.2</td></tr><tr><td>2</td><td>20.3</td></tr></table>

# 3.2.1 Which Earbud is Primary command

The Which Earbud is Primary command returns whether the primary is the Left or Right earbud. This can be used for example to match the serial number to the Left or Right earbud. 


Table 3-51 Which Earbud is Primary command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1+</td><td>20.2</td><td>All</td></tr></table>

# Command parameters

None 


Table 3-52 Which Earbud is Primary command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 or 1</td><td>0: Left
1: Right</td></tr></table>

# Possible feature-specific Error Status codes

None 

# 3.2.2 Get Secondary Serial Number command

The Get Secondary Serial Number command gets the customer-provided serial number for the secondary device. 


Table 3-53 Get Secondary Serial Number command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>1</td><td>2+</td><td>21.1</td><td>All</td></tr></table>

# Command Parameters

None 


Table 3-54 Get Secondary Serial Number Response PDU parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1+</td><td>-</td><td>Serial number text</td></tr></table>

If no serial number is set, then the Response PDU payload is empty. 

# Possible feature-specific Error Status codes

None 

# 3.2.3 Primary Earbud About To Change notification

The device can generate a notification when the primary device changes. This happens due to handover where the secondary earbuds have a better link to the mobile phone. 


Table 3-55 Primary Earbud About To Change notification


<table><tr><td>Notification ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1+</td><td>20.1</td><td>All</td></tr></table>


Table 3-56 Primary Earbud About to Change notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 or 1</td><td>0: Static handover of the GAIA link that is, it is disconnected, and the mobile app needs to reconnect it after waiting for five seconds. No state is retained in the device.
1: Dynamic handover o the GAIA link that is, it remains connected during handover but notifications need to be re-registered.</td></tr><tr><td>2</td><td>0 to 255</td><td>Time to delay any re-connection attempt to allow handover to complete. Value is in seconds.</td></tr></table>

# 3.2.4 Primary Earbud Changed notification

Primary Earbud Changed notification is sent by the primary earbud after the earbuds have changed roles. 


Table 3-57 Primary Earbud Changed notification


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>1</td><td>2</td><td>20.3</td><td>All</td></tr></table>


Table 3-58 Primary Earbud Changed notification parameters


<table><tr><td>Payload Byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 or 1</td><td>■ 1: Left
■ 0: Right</td></tr></table>

# 3.2.5 Secondary Earbud Connection State notification

Secondary Earbud Connection State notification is sent by the primary earbud when it connects to or disconnects from the secondary earbud. 


Table 3-59 Secondary Earbud Connection State notification


<table><tr><td>Notification ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>2</td><td>3</td><td>21.2</td><td>All</td></tr></table>


Table 3-60 Primary Earbud Changed notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 or 1</td><td>■ 1: Connected
■ 0: Disconnected</td></tr></table>

# 3.3 Voice UI (VA) feature


Table 3-61 VA commands


<table><tr><td>Feature ID</td><td>Version</td><td>ADK release</td></tr><tr><td>0x03</td><td>1</td><td>20.1</td></tr></table>

# 3.3.1 Get Selected Assistant command

The Get Selected Assistant command returns the currently active Voice Assistant. 


Table 3-62 Get Selected Assistant command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1+</td><td>20.1</td><td>All</td></tr></table>

# Command parameters

None 


Table 3-63 Get Selected Assistant Response PDU Contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>-</td><td>Identifier of currently selected assistant</td></tr></table>

Assistant identifiers in the range $0 \times 0 0$ to $\boldsymbol { 0 } \times 7 \boldsymbol { \mathrm { E } }$ are assigned by QTIL. IDs in the range $0 \times 8 0$ to 0xFF are for customers' own use. 


Table 3-64 Assistant identifiers


<table><tr><td>ID</td><td>Assistant</td></tr><tr><td>0x00</td><td>None</td></tr><tr><td>0x01</td><td>Reserved</td></tr><tr><td>0x02</td><td>GAA (Google)</td></tr><tr><td>0x03</td><td>AMA (Alexa)</td></tr></table>

# 3.3.2 Set Selected Assistant command

The Set Selected Assistant command selects the active Voice Assistant. 


Table 3-65 Set Selected Assistant command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>1</td><td>1+</td><td>20.1</td><td>All</td></tr></table>


Table 3-66 Set Selected Assistant parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>-</td><td>Identifier of assistant to be selected</td></tr></table>

Response PDU Contents 

None 

# 3.3.3 Get Supported Assistants command

The Get Supported Assistants command returns a list of supported Voice Assistants. The list will include $0 \mathbf { x } 0 0$ (‘none’) if the device supports switching to no assistant. 


Table 3-67 Get Supported Assistants command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>2</td><td>1+</td><td>20.1</td><td>All</td></tr></table>

Command parameters 

None 


Table 3-68 Get Supported Assistants Response PDU Contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>n</td><td>Number of supported assistants</td></tr><tr><td>2</td><td>-</td><td>Identifier of first assistant</td></tr><tr><td>...</td><td>...</td><td>...</td></tr><tr><td>n + 1</td><td>-</td><td>Identifier of nth assistant</td></tr></table>

# 3.3.4 Assistant Changed notification

Assistant Changed notification is sent when the active Voice Assistant is changed. 


Table 3-69 Assistant Changed notification


<table><tr><td>Notification ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1+</td><td>20.1</td><td>All</td></tr></table>


Table 3-70 Assistant Changed notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>-</td><td>Identifier of currently selected assistant.</td></tr></table>

# 3.4 Debug commands


Table 3-71 Debug commands


<table><tr><td>Feature ID</td><td>Version</td><td>ADK release</td></tr><tr><td rowspan="2">0x04</td><td>1</td><td>20.2</td></tr><tr><td>2</td><td>20.3</td></tr></table>

Debug commands provide two different debugging features: 

■ Reading debug information saved at a panic 

■ PyDbg access to the device 

# 3.4.1 Get Debug Log Info command

The Get Debug Log Info command returns the information about the debug partition and panic log stored in it. 


Table 3-72 Get Debug Log Info command


<table><tr><td>Command ID</td><td>ADK release</td><td>Variants</td></tr><tr><td>0x01</td><td>20.2</td><td>All</td></tr></table>


Table 3-73 Get Debug Log Info command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>(any)</td><td>Debug Log Info Key (MSB)</td></tr><tr><td>2</td><td>(any)</td><td>Debug Log Info Key (LSB)</td></tr></table>


Table 3-74 Debug Log Info Keys


<table><tr><td>Value</td><td>Comments</td></tr><tr><td>0x0000</td><td>Debug partition size</td></tr><tr><td>0x0001</td><td>Panic log size</td></tr></table>


Table 3-75 Get Debug Log Info Response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0</td><td>Debug Log Info Response PDU Version</td></tr><tr><td>2</td><td>(any)</td><td>Debug Log Info Key (MSB)</td></tr><tr><td>3</td><td>(any)</td><td>Debug Log Info Key (LSB)</td></tr><tr><td>4</td><td>(any)</td><td>Size of ‘debug partition’ or ‘panic log’ (MSB)</td></tr><tr><td>5</td><td>(any)</td><td>Size of ‘debug partition’ or ‘panic log’ (Second SB)</td></tr><tr><td>6</td><td>(any)</td><td>Size of ‘debug partition’ or ‘panic log’ (Third SB)</td></tr><tr><td>7</td><td>(any)</td><td>Size of ‘debug partition’ or ‘panic log’ (LSB)</td></tr></table>


Table 3-76 Get Debug Log Info command possible error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Invalid parameters</td><td>0x85</td><td>Invalid Debug Log Info Key.</td></tr><tr><td>No debug partition</td><td>0x86</td><td>The debug partition is not found. (The partition must be defined in the flash layout config file.)</td></tr><tr><td>Failure by unknown reason</td><td>0x8F</td><td>Unspecified reason caused the failure of the requested operation.</td></tr></table>

# 3.4.2 Configure Debug Logging command

The Configure Debug Logging command sets a debug_partition config parameter, with which the developer can configure the items of the debug information to be saved at a panic (see Using the Debug Partition Application Note). 

This command passes the Config Key and Config Value to the debug_partition configuration function on the device, and it receives the result. 


Table 3-77 Get Debug Logging command


<table><tr><td>Command ID</td><td>ADK release</td><td>Variants</td></tr><tr><td>0x02</td><td>20.2</td><td>All</td></tr></table>


Table 3-78 Configure Debug Logging command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>(any)</td><td>Debug Log Config Key (MSB)</td></tr><tr><td>2</td><td>(any)</td><td>Debug Log Config Key (LSB)</td></tr><tr><td>3</td><td>(any)</td><td>Debug Log Config Value (MSB)</td></tr><tr><td>4</td><td>(any)</td><td>Debug Log Config Value (Second SB)</td></tr><tr><td>5</td><td>(any)</td><td>Debug Log Config Value (Third SB)</td></tr><tr><td>6</td><td>(any)</td><td>Debug Log Config Value (LSB)</td></tr></table>


Table 3-79 Configure Debug Logging Response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>(any)</td><td>Debug Log Config Key (MSB)</td></tr><tr><td>2</td><td>(any)</td><td>Debug Log Config Key (LSB)</td></tr><tr><td>3</td><td>(any)</td><td>Debug Log Config Value (MSB)</td></tr><tr><td>4</td><td>(any)</td><td>Debug Log Config Value (Second SB)</td></tr><tr><td>5</td><td>(any)</td><td>Debug Log Config Value (Third SB)</td></tr><tr><td>6</td><td>(any)</td><td>Debug Log Config Value (LSB)</td></tr></table>


Table 3-80 Get Debug Log Info command possible error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Not enough space</td><td>0x82</td><td>No space is available to store panic log.</td></tr><tr><td>Invalid parameters</td><td>0x85</td><td>Invalid Debug Log Config Key.</td></tr><tr><td>No debug partition</td><td>0x86</td><td>The debug partition is not found. (The partition must be defined in the flash layout config file.)</td></tr></table>

# 3.4.3 Get Panic Log command

The Get Panic Log command initiate the transfer of the panic log from the device. This command must be followed by Data Transfer Setup command to set up the data transfer path, and then Data Transfer Get command to read panic log from the device. See sections Data Transfer Setup command and Data Transfer Get command. 

A Session ID created by the device is notified by the response of this command, and the Session ID is required by both Data Transfer Setup/Get commands. 


Table 3-81 Get Panic Log command


<table><tr><td>Command ID</td><td>ADK release</td><td>Variants</td></tr><tr><td>0x03</td><td>20.2</td><td>All</td></tr></table>

# Command parameters

None 


Table 3-82 Get Panic Log Response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>(any)</td><td>Session ID (MSB)</td></tr><tr><td>2</td><td>(any)</td><td>Session ID (LSB)</td></tr><tr><td>3</td><td>(any)</td><td>Size of panic log in bytes (MSB)</td></tr><tr><td>4</td><td>(any)</td><td>Size of panic log in bytes (Second SB)</td></tr><tr><td>5</td><td>(any)</td><td>Size of panic log in bytes (Third SB)</td></tr><tr><td>6</td><td>(any)</td><td>Size of panic log in bytes (LSB)</td></tr></table>


Table 3-83 Get Panic Log command possible error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>No data</td><td>0x81</td><td>The debug partition is empty.</td></tr><tr><td>Invalid parameters</td><td>0x85</td><td>Invalid parameters(s).</td></tr><tr><td>No debug partition</td><td>0x86</td><td>The debug partition is not found. (The partition must be defined in the flash layout config file.)</td></tr><tr><td>Failure by unknown reason</td><td>0x8F</td><td>Unspecified reason caused the failure of the requested operation.</td></tr></table>

# 3.4.4 Erase Panic Log command

The Erase Log Info command erases the entire debug partition. 


Table 3-84 Erase Panic Log command


<table><tr><td>Command ID</td><td>ADK release</td><td>Variants</td></tr><tr><td>0x04</td><td>20.2</td><td>All</td></tr></table>


Table 3-85 Erase Panic Log command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x00</td><td>Reserved for Future Use (MSB)</td></tr><tr><td>2</td><td>0x00</td><td>Reserved for Future Use (LSB)</td></tr></table>

These parameters must be set to zero to erase the debug_partition. 

# Response PDU Contents

None 


Table 3-86 Erase Panic Log command possible error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Busy</td><td>0x83</td><td>The resource (the debug partition) is busy.</td></tr><tr><td>Invalid parameters</td><td>0x85</td><td>Invalid Debug Log Info Key.</td></tr><tr><td>No debug partition</td><td>0x86</td><td>The debug partition is not found. (The partition must be defined in the flash layout config file.)</td></tr><tr><td>Failure by unknown reason</td><td>0x8F</td><td>Unspecified reason caused the failure of the requested operation.</td></tr></table>

# 3.4.5 Debug Tunnel To Chip command

The Debug Tunnel To Chip command provides a tunnel between the device and the mobile app. 


Table 3-87 Debug Tunnel To Chip command


<table><tr><td>Command ID</td><td>ADK release</td><td>Variants</td></tr><tr><td>0x05</td><td>20.2</td><td>All</td></tr></table>


Table 3-88 Debug Tunnel To Chip command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>(any)</td><td>Client ID</td></tr><tr><td>2</td><td>(any)</td><td>Tag</td></tr><tr><td>3</td><td>(any)</td><td>Tunnelling payload (1/N)</td></tr><tr><td>…</td><td>…</td><td>…</td></tr><tr><td>N + 2</td><td>(any)</td><td>Tunnelling payload (N/N)</td></tr></table>


Table 3-89 Debug Tunnel To Chip response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>(any)</td><td>Client ID</td></tr><tr><td>2</td><td>(any)</td><td>Tag</td></tr><tr><td>3</td><td>(any)</td><td>Tunnelling payload (1/N)</td></tr><tr><td>...</td><td>...</td><td>...</td></tr><tr><td>N + 2</td><td>(any)</td><td>Tunnelling payload (N/N)</td></tr></table>


Table 3-90 Debug Tunnel To Chip command possible error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Invalid parameters</td><td>0x85</td><td>Invalid parameter(s).</td></tr><tr><td>Failure by unknown reason</td><td>0x8F</td><td>Unspecified reason caused the failure of the requested operation.</td></tr></table>

# 3.4.6 GAIA Debug Error status code

The GAIA Debug feature commands could return error responses with the status code below. 


Table 3-91 GAIA Debug error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>No data</td><td>0x81</td><td>The debug partition is empty.</td></tr><tr><td>Not enough space</td><td>0x82</td><td>No space is available to store panic log.</td></tr><tr><td>Busy</td><td>0x83</td><td>The resource (the debug partition) is busy.</td></tr><tr><td>Invalid command</td><td>0x84</td><td>Invalid or not supported command ID.</td></tr><tr><td>Invalid parameters</td><td>0x85</td><td>Invalid parameters.</td></tr><tr><td>No debug partition</td><td>0x86</td><td>The debug partition is not found.</td></tr><tr><td>Unknown error</td><td>0x8F</td><td>Unspecified reason caused the failure of the requested operation.</td></tr></table>

If the mobile app sends a GAIA Debug feature command that is invalid or not supported, instead of the GAIA Debug feature, the GAIA Framework sends the error response to the mobile app with the error code of $0 \times 0 1$ Command not supported. 

# 3.5 Music Processing feature

The Music Processing GAIA feature is enabled by default on Headset Applications but not Earbud Applications. To enable this feature on the Earbud Application, add into the project definitions: 

```c
define INCLUDE_MUSIC_PROCESSING #define INCLUDE_MUSIC_PROCESSING_PEER 
```

# 3.5.1 Get EQ State command

The GAIA client uses the Get EQ State command to decide whether the user can interact with the User EQ settings (predefined or user set). 

The response contains whether the User EQ is present in the running audio chain or not. 


Table 3-92 Get EQ State command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1</td><td>20.3</td><td>All</td></tr></table>

Command parameters 

None 


Table 3-93 Get EQ State parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 or 1</td><td>■ 0: Not present in chain
■ 1: Present in chain</td></tr></table>

Possible Feature specific Error Status codes 

None 

# 3.5.2 Get Available EQ Pre-sets command

GAIA client uses the Get Available EQ Pre-sets command to find out the IDs of the supported presets. Each preset is identified by a number, which you must convert to a string and present to the user. 


Table 3-94 Get Available EQ Pre-sets command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>1</td><td>1+</td><td>20.3</td><td>All</td></tr></table>

The response contains a list of IDs supported by the application. This set will include the Off, User and the defined preset IDs. These IDs will be used by other commands/notifications 

Command Parameters 

None 


Table 3-95 Get Available EQ response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 to 63</td><td>Number of presets (including off and user)</td></tr><tr><td>2</td><td>0 to 63</td><td>0: Off</td></tr><tr><td>3</td><td>0 to 63</td><td>1: Pre-set</td></tr><tr><td>…</td><td>…</td><td>…</td></tr><tr><td>n</td><td>0 to 63</td><td>3F: User</td></tr></table>

Possible feature-specific Error Status codes 

None 

# 3.5.3 Get EQ Set command

GAIA client uses the Get EQ Set command to find out what the currently selected preset (or User or off) is. 


Table 3-96 Get EQ Set command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>2</td><td>1+</td><td>20.3</td><td>All</td></tr></table>

The response contains Off, User Set or Pre-set ID. Off means the EQ is in pass-through. 

Command Parameters 

None 


Table 3-97 Get EQ Set response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 to 63</td><td>0: Off
1: Pre-set
3F: User</td></tr></table>

Possible feature-specific Error Status codes 

None 

# 3.5.4 Set EQ Set command

GAIA client uses the Set EQ Set command to set the new preset value or user set or Off. 


Table 3-98 Set EQ Set command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>3</td><td>1+</td><td>20.3</td><td>All</td></tr></table>

This command contains Off, User Set or Pre-set ID. Off sets the EQ into pass-through. 


Table 3-99 Set EQ Set command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 to 63</td><td>■ 0: Off
■ 1: Pre-set
■ 3F: User</td></tr></table>


Table 3-100 Set EQ Set response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>1</td><td>Ack</td></tr></table>

Possible feature-specific Error Status codes 

None 

# 3.5.5 Get User Set Number Of Bands command

GAIA client uses the Get User Set Number of Bands command to find out how many frequency bands the user set supports. 


Table 3-101 Get User Set Number of Bands command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>4</td><td>1+</td><td>20.3</td><td>All</td></tr></table>

Command Parameters 

None 


Table 3-102 Get User Set Number of Bands response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 to 9</td><td>Number of bands</td></tr></table>

Possible feature-specific Error Status codes 

None 

# 3.5.6 Get User Set Configuration command

GAIA client uses the Get User Set Configuration command to find out the details of each band. 


Table 3-103 Get User Set Configuration command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>5</td><td>1+</td><td>20.3</td><td>All</td></tr></table>


Table 3-104 Get User Set Configuration command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 to 9</td><td>Start band</td></tr><tr><td>2</td><td>0 to 9</td><td>End band</td></tr></table>


Table 3-105 Get User Set Configuration Response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 to 9</td><td>Start band</td></tr><tr><td>2</td><td>0 to 9</td><td>End band</td></tr><tr><td>3 to 4</td><td>0 to 65535</td><td>Centre frequency</td></tr><tr><td>5-6</td><td>0 to 65535</td><td>Q</td></tr><tr><td>7</td><td>0 to 255</td><td>Filter type</td></tr><tr><td>8 to 9</td><td>0 to 65535</td><td>Gain value</td></tr><tr><td>...</td><td>...</td><td>...</td></tr><tr><td>66 to 67</td><td>0 to 65535</td><td>Centre frequency</td></tr><tr><td>68 to 69</td><td>0 to 65535</td><td>Q</td></tr><tr><td>70</td><td>0 to 255</td><td>Filter type</td></tr><tr><td>71 to 72</td><td>0 to 65535</td><td>Gain value</td></tr></table>

# Possible-feature specific Error Status codes

None 

# 3.5.7 Set User Set Configuration command

GAIA client uses the Set User Set Configuration command to set the gains of a specific set of bands. 


Table 3-106 Set User Set Configuration command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>6</td><td>1+</td><td>20.3</td><td>All</td></tr></table>


Table 3-107 Set User Set command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 to 9</td><td>Start band</td></tr><tr><td>2</td><td>0 to 9</td><td>End band</td></tr><tr><td>3 to 4</td><td>0 to 65535</td><td>Gain value</td></tr><tr><td>…</td><td>…</td><td>…</td></tr><tr><td>23 to 24</td><td>0 to 65535</td><td>Gain value</td></tr></table>


Table 3-108 Set User Set Response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>1</td><td>Ack</td></tr></table>

Possible feature-specific Error Status codes 

None 

# 3.5.8 EQ State Change notification

GAIA client will be told if the User EQ is not present (for example, if a headset was streaming music so the User EQ was present but the phone started ringing which removes the User EQ). 


Table 3-109 EQ Change notification


<table><tr><td>Notification ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1+</td><td>20.3</td><td>All</td></tr></table>


Table 3-110 EQ State Change notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 or 1</td><td>■ 0: Not present
■ 1: Present</td></tr></table>

# 3.5.9 EQ Set Change notification

GAIA client will be told if the User EQ set (preset, User set or Off) changes. 


Table 3-111 EQ Set Change notification


<table><tr><td>Notification ID</td><td>Version</td><td>ADK Release</td><td>Variants</td></tr><tr><td>1</td><td>1+</td><td>20.3</td><td>All</td></tr></table>


Table 3-112 EQ Set Change notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 to 63</td><td>■ 0: Off
■ 1: Pre-set
■ 3F: User</td></tr></table>

# 3.5.10 User EQ Band Change notification

This notification tells the GAIA client if there are User EQ band changes. 


Table 3-113 User EQ Band Change notification


<table><tr><td>Notification ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>2</td><td>1+</td><td>20.3</td><td>All</td></tr></table>


Table 3-114 User EQ Band Change notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0-9</td><td>Number of bands changed</td></tr><tr><td>2</td><td>0-9</td><td>Changed band number</td></tr><tr><td>…</td><td>…</td><td>…</td></tr><tr><td>10</td><td>0-9</td><td>Changed band number</td></tr></table>

# 3.6 Device Firmware Upgrade (DFU) feature


Table 3-115 DFU commands


<table><tr><td>Feature ID</td><td>Version</td><td>ADK release</td></tr><tr><td rowspan="2">0x06</td><td>1</td><td>20.1</td></tr><tr><td>2</td><td>20.3</td></tr></table>

# 3.6.1 Upgrade Connect command

The Upgrade Connect command connects a GAIA transport to the upgrade library. 


Table 3-116 Upgrade Connect command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1+</td><td>20.1</td><td>All</td></tr></table>

Command parameters 

None 

Response PDU contents 

None 

Possible Feature specific Error Status codes 

None 

# 3.6.2 Upgrade Disconnect command

The Upgrade Disconnect command disconnects a GAIA transport from the upgrade library. 


Table 3-117 Upgrade Disconnect command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>1</td><td>1+</td><td>20.1</td><td>All</td></tr></table>

Command parameters 

None 

Response PDU contents 

None 

# Possible Feature specific Error Status codes

None 

# 3.6.3 Upgrade Control command

The Upgrade Control command tunnels Upgrade messages to the device. 


Table 3-118 Upgrade Control command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>2</td><td>1+</td><td>20.1</td><td>All</td></tr></table>

# Command parameters

None 

# Response PDU contents

None 

# Possible Feature specific Error Status codes

None 

# 3.6.4 Get Data Endpoint Mode command

The Get Data Endpoint Mode command returns the data endpoint that is set. 


Table 3-119 Get Data Endpoint Mode command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>3</td><td>1+</td><td>20.1</td><td>All</td></tr></table>

# Command parameters

None 


Table 3-120 Get Data Endpoint Mode Response PDU parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 or 1</td><td>Data endpoint mode:
■ 0 : None
■ 1: RWCP</td></tr></table>

# Possible Feature specific Error Status codes

None 

# 3.6.5 Set Data Endpoint Mode command

The Set Data Endpoint Mode command sets the endpoint to use for sending data during an upgrade. 


Table 3-121 Set Data Endpoint Mode command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>4</td><td>1+</td><td>20.1</td><td>All</td></tr></table>


Table 3-122 Set Data Endpoint Mode command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 or 1</td><td>Data endpoint mode:
■ 0: None
■ 1: RWCP</td></tr></table>

Response PDU contents 

None 

Possible Feature-specific Error Status codes 

None 

# 3.6.6 Upgrade Data Indication notification

The Upgrade Data Indication notification tunnels Upgrade messages from the device. 


Table 3-123 Upgrade Data Indication notification


<table><tr><td>Notification ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1+</td><td>20.1</td><td>All</td></tr></table>


Table 3-124 Upgrade Data Indication notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>Data</td><td>Upgrade data</td></tr></table>

# 3.6.7 Upgrade Stop Request notification

Upgrade Stop Request notification indicates that device would like upgrade to stop. 


Table 3-125 Upgrade Stop Request notification


<table><tr><td>Notification ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>2+</td><td>20.3</td><td>All</td></tr></table>


Table 3-126 Upgrade Stop Request notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>Action</td><td>■ 0x00: Disconnect upgrade
■ 0x01: Stop sending data (pause)</td></tr></table>

# 3.6.8 Upgrade Start Request notification

This notification indicates that device is ready to restart or resume an upgrade. 


Table 3-127 Upgrade Start Request notification


<table><tr><td>Notification ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>2+</td><td>20.3</td><td>All</td></tr></table>


Table 3-128 Upgrade Start Request notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>Action</td><td>■ 0x00: (Re)connect upgrade
■ 0x01: Start sending data (resume)</td></tr></table>

# 3.7 Handset service feature

# 3.7.1 Enable Multipoint command

The GAIA Client uses this command to switch between single-point and multi-point. 


Table 3-129 Enable Multipoint command


<table><tr><td>Command ID</td><td>Verison</td><td>ADK Release</td><td>Variants</td></tr><tr><td>0</td><td>1</td><td>TBD</td><td>All</td></tr></table>


Table 3-130 Enable Multipoint command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 or 1</td><td>0 = Single-point
1 = Multi-point</td></tr></table>

# Response PDU contents

None 

Possible feature specific error status codes 

None 

# 3.7.2 Multipoint Enabled Changed notification

The GAIA Client will be told if multipoint has been enabled or disabled. 


Table 3-131 Multipoint Enabled Changed notification


<table><tr><td>Notification ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1+</td><td>TBD</td><td>All</td></tr></table>


Table 3-132 Multipoint Enabled Changed notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 or 1</td><td>0 = Disabled
1 = Enabled</td></tr></table>

# 3.8 Audio Curation (AC) feature


Table 3-133 AC commands


<table><tr><td>Feature ID</td><td>Version</td><td>ADK release</td></tr><tr><td>0x08</td><td>1</td><td>21.1</td></tr></table>

# 3.8.1 Get State command

The Get State command provides the current state of the device Audio Curation feature type. 


Table 3-134 Get State command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1</td><td>21.1</td><td>All</td></tr></table>


Table 3-135 Get State command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td></td><td>Feature type</td></tr><tr><td>0x01</td><td>ANC</td></tr></table>

# Response PDU contents

Returns the state of the AC feature type. 


Table 3-136 Get State response parameters


<table><tr><td>Payload Byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td></td><td>Feature type</td></tr><tr><td>0x01</td><td>ANC</td></tr><tr><td rowspan="2">2</td><td>0x00</td><td>Disable</td></tr><tr><td>0x01</td><td>Enable</td></tr></table>

# 3.8.2 Set State command

The Set State command allows the AC feature type to be turned On and Off. 


Table 3-137 Set State command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>1</td><td>1</td><td>21.1</td><td>All</td></tr></table>


Table 3-138 Set State command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td></td><td>Feature type</td></tr><tr><td></td><td>0x01</td><td>ANC</td></tr><tr><td rowspan="2">2</td><td>0x00</td><td>Disable</td></tr><tr><td>0x01</td><td>Enable</td></tr></table>

Response PDU contents 

None 

Possible Feature-specific Error status codes 

None 

# 3.8.3 Get Modes Count command

The device returns the number of modes configured in the device. Modes are a predefined set of filter coefficients and related parameters for audio curation. 


Table 3-139 Get Modes Count command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>2</td><td>1</td><td>21.1</td><td>All</td></tr></table>

Command parameters 

None 

Response PDU contents 

Returns the number modes configured in the device. 


Table 3-140 Get Num Modes response parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x01 to 0x7f</td><td>Number of configured modes</td></tr></table>

Possible Feature-specific Error status codes 

None 

# 3.8.4 Get Current Mode command

The device returns the configured mode currently set. 


Table 3-141 Get Current ANC Mode command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>3</td><td>1</td><td>21.1</td><td>All</td></tr></table>

Command parameters 

None 

Response PDU contents 

Returns the current mode and associated mode information. 


Table 3-142 Get Current ANC Mode response parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x01 to 0x7f</td><td>Current mode</td></tr><tr><td>2</td><td>0x01 to 0xff</td><td>Feature type: 
■ 0x01: Static ANC 
■ 0x02: Leakthrough ANC 
■ 0x03: Adaptive ANC 
■ 0x04 to 0xff: Reserved</td></tr><tr><td>3</td><td>0x00 to 0x01</td><td>Adaptation control support: 
■ 0x00: Not supported 
■ 0x01: Supported</td></tr><tr><td>4</td><td>0x00 to 0x01</td><td>Gain control support: 
■ 0x00: Not supported 
■ 0x01: Supported</td></tr></table>

# 3.8.5 Set Mode command

The Set Mode command configures the device to the set filter coefficients that corresponds to the provided Mode ID. 


Table 3-143 Set Mode command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>4</td><td>1</td><td>21.1</td><td>All</td></tr></table>


Table 3-144 Set Mode command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x01 to 0x7f</td><td>Mode to set</td></tr></table>

Response PDU contents 

None 

# 3.8.6 Get Gain command

The Get Gain command returns the gain of the device. 


Table 3-145 Get Gain command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>5</td><td>1</td><td>21.1</td><td>All</td></tr></table>

Command parameters 

None 


Table 3-146 Get Gain response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x01 to 0x7f</td><td>Current mode</td></tr><tr><td>2</td><td>0x01 to 0xff</td><td>Feature type: 
■ 0x01: Static ANC 
■ 0x02: Leakthrough ANC 
■ 0x03: Adaptive ANC 
0x04 to 0xff: Reserved</td></tr><tr><td>3</td><td>0x00 to 0xff</td><td>Left gain value</td></tr><tr><td>4</td><td>0x00 to 0xff</td><td>Right gain value</td></tr></table>


Table 3-147 Get Gain possible Error status code


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Incorrect state</td><td>0x06</td><td>The device is not in the correct state to process the command.</td></tr><tr><td>Invalid parameter</td><td>0x05</td><td>An invalid parameter was used in the command</td></tr></table>

# 3.8.7 Set Gain command

The Set Gain command sets the gain of the device to the provided value. This is only applicable to modes where gain change is allowed by the user, for example, Leakthrough ANC modes. 


Table 3-148 Set Gain command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>6</td><td>1</td><td>21.1</td><td>All</td></tr></table>


Table 3-149 Set Gain command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x00 to 0xff</td><td>Left gain value</td></tr><tr><td>2</td><td>0x00 to 0xff</td><td>Right gain value</td></tr></table>

NOTE (1) The gain values can be changed for Leakthrough mode. The gain values must be the same for Left and Right in this case. Returns invalid parameter if the values are different. (2) The gain values cannot be changed for Static and Adaptive modes. The device returns Incorrect State if requested for these modes. 

# Response PDU contents

None 


Table 3-150 Set Gain possible Error status code


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Incorrect state</td><td>0x06</td><td>The device is not in the correct state to process the command.</td></tr><tr><td>Invalid parameter</td><td>0x05</td><td>An invalid parameter was used in the command</td></tr></table>

# 3.8.8 Get Toggle Configuration Count command

The Get Toggle Configuration Count command provides the number of toggle configurations the device supports. 


Table 3-151 Get Toggle Configuration Count command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>7</td><td>1</td><td>21.1</td><td>RDP</td></tr></table>

# Command parameters

None 


Table 3-152 Get Toggle Configuration Count response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x00 to 0x7f</td><td>Number of toggle options that are supported.</td></tr></table>

# 3.8.9 Get Toggle Configuration command

The Get Toggle Configuration command gets the option that is set for a toggle configuration. 


Table 3-153 Get Toggle Configuration command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>8</td><td>1</td><td>21.1</td><td>All</td></tr></table>


Table 3-154 Get Toggle Configuration command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x01 to 0x7f</td><td>Toggle option number</td></tr></table>


Table 3-155 Get Toggle Configuration response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x01 to 0x7f</td><td>Toggle option number</td></tr><tr><td>2</td><td>0x00 to 0xff</td><td>Toggle option value: 
■ 0x00: Off 
■ 0x01 to 0x7f: Configured mode 
■ 0x80 to 0xFE: Reserved 
■ 0xff: Void/Toggle option not configured</td></tr></table>


Table 3-156 Get Toggle Configuration possible Error status code


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Invalid parameter</td><td>0x05</td><td>An invalid parameter was used in the command</td></tr></table>

# 3.8.10 Set Toggle Configuration command

The Set Toggle Configuration command sets the toggle option the user can toggle using controls on the device. 


Table 3-157 Set Toggle Configuration command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>9</td><td>1</td><td>21.1</td><td>All</td></tr></table>


Table 3-158 Set Toggle Configuration command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x01 to 0x7f</td><td>ANC Toggle option number</td></tr><tr><td>2</td><td>0x00 to 0xff</td><td>ANC Toggle option value: 
■ 0x00: Off 
■ 0x01 to 0x7f: Configured mode 
■ 0x80 to 0xFE: Reserved 
■ 0xff: Void/Not connected</td></tr></table>

# Response PDU contents

None 


Table 3-159 Set Toggle Configuration possible Error status code


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Invalid parameter</td><td>0x05</td><td>An invalid parameter was used in the command</td></tr></table>

# 3.8.11 Get Scenario Configuration command

The Get Scenario Configuration command gets the behavior configured for a specific scenario on the device. 


Table 3-160 Get Scenario Configuration command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>10</td><td>1</td><td>21.1</td><td>All</td></tr></table>


Table 3-161 Get Scenario Configuration command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x00 to 0xff</td><td>Scenario:
■ 0x01: Idle
■ 0x02:Playback/Music
■ 0x03: Voice call
■ 0x04: Digital assistant
■ 0x05: Reserved</td></tr></table>


Table 3-162 Get Scenario Configuration response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x00 to 0xff</td><td>Scenario:
■ 0x01: Idle
■ 0x02: Playback/Music
■ 0x03: Voice call</td></tr><tr><td></td><td></td><td>■ 0x04: Digital assistant
■ 0x05: Reserved</td></tr><tr><td>2</td><td>0x01 to 0xff</td><td>Behavior:
■ 0x00: Off
■ 0x01 to 0x7f: Configured mode
■ 0x80 to 0xFE: Reserved
■ 0xff: Same as current</td></tr></table>


Table 3-163 Get Scenario Configuration possible Error status code


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Invalid parameter</td><td>0x05</td><td>An invalid parameter was used in the command</td></tr></table>

# 3.8.12 Set Scenario Configuration command

The Set Scenario Configuration command sets the behavior to be used when in different scenarios, for example, idle state, voice calls or digital assistant use. 


Table 3-164 Set Scenario Configuration command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>11</td><td>1</td><td>21.1</td><td>All</td></tr></table>


Table 3-165 Set Scenario Configuration command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x00 to 0xff</td><td>Scenario: 
■ 0x01: Idle 
■ 0x02: Playback/Music 
■ 0x03: Voice call 
■ 0x04: Digital assistant 
■ 0x05: Reserved</td></tr><tr><td>2</td><td>0x01 to 0xff</td><td>Behavior: 
■ 0x00: Off 
■ 0x01 to 0x7f: Configured mode 
■ 0x80 to 0xFE: Reserved 
■ 0xff: Same as current</td></tr></table>


Response PDU contents


None 


Table 3-166 Set Scenario Configuration possible Error status code


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Invalid parameter</td><td>0x05</td><td>An invalid parameter was used in the command</td></tr></table>

# 3.8.13 Get Demo Support command

The Get Demo Support command is used to identify if the device supports demonstration state. 


Table 3-167 Get Demo Support command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>12</td><td>1</td><td>21.1</td><td>RDP</td></tr></table>

Command parameters 

None 


Table 3-168 Get Demo Support response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td>0x00</td><td>Not supported</td></tr><tr><td>0x01</td><td>Supported</td></tr></table>

# 3.8.14 Get Demo State command

The Get Demo State command is used to identify if the device is currently in a demonstration state. 


Table 3-169 Get Demo State command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>13</td><td>1</td><td>21.1</td><td>RDP</td></tr></table>

Command parameter 

None 


Table 3-170 Get Demo State command response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td>0x00</td><td>Out of Demo mode</td></tr><tr><td>0x01</td><td>In Demo mode</td></tr></table>

# 3.8.15 Set Demo State command

The Set Demo State command lets the device know if AC is to be moved into or out of demo. In demo user has more options to control the AC behavior allowing them to get a flavor of AC functionality supported. 

Device can move out of demo mode on GAIA disconnect. 


Table 3-171 Set Demo State command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>14</td><td>1</td><td>21.1</td><td>RDP</td></tr></table>


Table 3-172 Set Demo Support command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td>0x00</td><td>Exit Demo mode</td></tr><tr><td>0x01</td><td>Enter Demo mode</td></tr></table>

Response PDU contents 

None 


Table 3-173 Set Demo State command possible error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Incorrect state</td><td>0x06</td><td>The device is not the correct state to process the command.</td></tr></table>

# 3.8.16 Get Adaptation Control Status command

The Get Adaptation Control Status command gets the state of gain adaptation. 

It is only applicable if the device is in Demo mode. 


Table 3-174 Get Adaptation Control Status command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>15</td><td>1</td><td>21.1</td><td>All</td></tr></table>

Command parameters 

None 


Table 3-175 Get Adaptation Control Status response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td>0x00</td><td>Adaptation suspended</td></tr><tr><td>0x01</td><td>Adaptation running</td></tr></table>


Table 3-176 Get Adaptation Control Status possible error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Incorrect state</td><td>0x06</td><td>The device is not in the correct state to process the command.</td></tr></table>

# 3.8.17 Set Adaptation Control Status command

The Set Adaptation Control Status command sets the state of gain adaptation. 

It is only applicable if the device is in Demo mode. 


Table 3-177 Set Adaptation Control Status command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>16</td><td>1</td><td>21.1</td><td>All</td></tr></table>


Table 3-178 Set Adaptation Control Status command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td>0x00</td><td>Pause/Suspend adaptation</td></tr><tr><td>0x01</td><td>Resume adaptation</td></tr></table>

# Response PDU contents

None 


Table 3-179 Set Adaptation Control Status possible error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Incorrect state</td><td>0x06</td><td>The device is not in the correct state to process the command.</td></tr></table>

# 3.8.18 Get Leakthrough dB Gain Slider Configuration command

This command provides the slider configuration supported by device for current mode. It gets the number of steps in which leakthrough dB gain can be configured, dB value of each step, minimum dB gain that can be configured and the step corresponding to current leakthrough dB gain. 


Table 3-180 Get Leakthrough dB Gain Slider Configuration command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>17</td><td>2</td><td>21.4</td><td>All</td></tr></table>

# Command parameters

None 


Table 3-181 Get Leakthrough dB Gain Slider Configuration command response PDU contents


<table><tr><td>Payloa
d byte</td><td>Value</td><td colspan="3">Comments</td></tr><tr><td>1</td><td>0x01 to 0x7F</td><td colspan="3">Current mode</td></tr><tr><td>2</td><td>0x03 to 0x0A</td><td colspan="3">Number of steps that are supported</td></tr><tr><td>Payloadd byte</td><td>Value</td><td colspan="3">Comments</td></tr><tr><td>3</td><td>0x02 to0x05</td><td colspan="3">dB step size in which leukthrough gain can be updated</td></tr><tr><td rowspan="8">4</td><td rowspan="8">0x00 to0xFF</td><td colspan="3">Minimum leukthrough gain supported by device.Payload represents an 8-bit signed representation of minimum leukthrough dB gain(-128 dB to 127 dB)</td></tr><tr><td>Payload (bin)</td><td>Payload (hex)</td><td>dB gain</td></tr><tr><td>0000 0000</td><td>0x00</td><td>0</td></tr><tr><td>0000 1010</td><td>0xA</td><td>10</td></tr><tr><td>0111 1111</td><td>0x7F</td><td>127</td></tr><tr><td>1000 0000</td><td>0x80</td><td>-128</td></tr><tr><td>1111 0110</td><td>0xF6</td><td>-10</td></tr><tr><td>1111 1111</td><td>0xFF</td><td>-1</td></tr><tr><td>5</td><td>0x01 to0x0A</td><td colspan="3">Current step at which leukthrough slider points to</td></tr></table>

NOTE 1. This command is only applicable for static/adaptive leakthrough mode. The device returns incirrect State if requested for other mode configurations. 

2. Adaptive leakthrough mode is not supported on QCC514x, QCC515x, QCC304x, QCC305x devices. 

3. This configuration may differ for each leakthrough mode 

4. Leakthrough dB gain can be obtained from current step using formula: MinGain $=$ ((CurStep - 1) * StepSize) 


Table 3-182 Get Leakthrough dB Gain Slider Configuration command possible feature specific error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Incorrect state</td><td>0x06</td><td>This device is not in the correct state to process the command.</td></tr></table>

# 3.8.19 Get Current Leakthrough dB Gain Step command

The Get Current Leakthrough dB Gain Step command provides the current step corresponding to leakthrough gain with which device is configured. 


Table 3-183 Get Current Leakthrough dB Gain Step command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>18</td><td>2</td><td>21.4</td><td>All</td></tr></table>

Command parameters 

# None


Table 3-184 Get Current Leakthrough dB Gain Step command response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x01 to 0x7f</td><td>Current mode</td></tr><tr><td>2</td><td>0x01 to 0x0A</td><td>Current step at which leakthrough slider point to</td></tr></table>

NOTE 1. This command is only applicable for static/adaptive leakthrough mode. The device returns incorrect State if requested for other mode configurations. 

2. Adaptive leakthrough mode is not supported on QCC514x, QCC515x, QCC304x, QCC305x devices 

3. Leakthrough dB gain can be obtained from current step using formula: MinGain $^ +$ ((CurStep - 1) * StepSize) 


Table 3-185 Get Current Leakthrough dB Gain Step command response PDU contents


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Incorrect state</td><td>0x06</td><td>The device is not in the correct state to process the command.</td></tr></table>

# 3.8.20 Set Leakthrough dB Gain Step command

The Set Leakthrough dB Gain Step command sets the leakthrough gain on the device corresponding to step value provided by user. 


Table 3-186 Set Leakthrough dB Gain Step command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>19</td><td>2</td><td>21.4</td><td>All</td></tr></table>


Table 3-187 Set Leakthrough dB Gain Step command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x01 to 0x0A</td><td>Step value to which leakthrough gain is to be updated</td></tr></table>

# Response PDU contents

# None

NOTE 1. If the step value exceeds maximum supported value or goes below minimum supported value, device returns Invalid Parameter error code. 

2. This command is only applicable for static/adaptive leakthrough mode. The device returns Incorrect State if requested for other mode configurations. 

3. Adaptive leakthrough mode is not supported on QCC514x, QCC515x, QCC304x, QCC305x devices. 

4. Leakthrough dB gain can be obtained from step using formula: MinGain $^ +$ ( (Step - 1) * StepSize) 


Table 3-188 Set Leakthrough dB Gain Step command possible feature specific error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Incorrect state</td><td>0x06</td><td>The device is not in the correct state to process the command.</td></tr><tr><td>Invalid parameter</td><td>0x05</td><td>An invalid parameter was used in the command.</td></tr></table>

# 3.8.21 Get Left Right Balance command

The Get Left Right Balance command provides the current setting of balance with which device is configured. 


Table 3-189 Get Left Right Balance command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>20</td><td>2</td><td>21.4</td><td>All</td></tr></table>

Command parameters 

None 


Table 3-190 Get Left Right Balance command response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td>0x00</td><td>Left</td></tr><tr><td>0x01</td><td>Right</td></tr><tr><td>2</td><td>0x00 to 0x064</td><td>Gain</td></tr></table>

NOTE 1. This command is only applicable for static/adaptive leakthrough mode. The device returns Incorrect State if requested for other mode configurations. 

2. Adaptive leakthrough mode is not supported on QCC514x, QCC515x, QCC304x, QCC305x devices. 

3. This balance remains constant across all leakthrough modes. 


Table 3-191 Get Left Right Balance command possible feature specific error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Incorrect state</td><td>0x06</td><td>The device is not in the correct state to process the command.</td></tr></table>

# 3.8.22 Set Left Right Balance command

The Set Left Right Balance command sets the balance of the device to the provided value. This is only applicable to modes where gain change is allowed by the user, for example, Static/Adaptive Leakthrough ANC modes. This balance will remain constant across all leakthrough modes. 


Table 3-192 Set Left Right Balance command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>21</td><td>2</td><td>21.4</td><td>All</td></tr></table>


Table 3-193 Set Left Right Balance command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td>0x00</td><td>Left</td></tr><tr><td>0x01</td><td>Right</td></tr><tr><td>2</td><td>0x00 to 0x064</td><td>Gain</td></tr></table>

# Response PDU contents

None 

NOTE 1. The gain values must be in the range of 0 to 100 defining the percentage of balance. Returns invalid parameter if the values are different. 

2. The gain values cannot be changed for Static and Adaptive modes. The device returns Incorrect State if requested for these modes. 


Table 3-194 Set Left Right Balance command possible feature specific error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Incorrect state</td><td>0x06</td><td>The device is not in the correct state to process the command.</td></tr><tr><td>Invalid parameter</td><td>0x05</td><td>An invalid parameter was used in the command</td></tr></table>

# 3.8.23 Get Wind Noise Reduction Support command

This command is used to identify if the device supports Wind Noise Reduction feature. 


Table 3-195 Get Wind Noise Reduction Support command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>22</td><td>2</td><td>21.4</td><td>QCC517x/307x</td></tr></table>

Command parameters 

None 


Table 3-196 Get Wind Noise Reduction Support command response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td>0x00</td><td>Not supported</td></tr><tr><td>0x01</td><td>Supported</td></tr></table>

Possible feature specific error codes 

None 

# 3.8.24 Get Wind Noise Detection State command

This command provides the current state of the Wind Noise Reduction feature on device. 


Table 3-197 Get Wind Noise Detection State command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>23</td><td>2</td><td>21.4</td><td>QCC517x/307x</td></tr></table>

Command parameters 

None 

Response PDU contents 

Returns the state of the Wind Noise Reduction feature state. 


Table 3-198 Get Wind Noise Detection State command response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td>0x00</td><td>Disable</td></tr><tr><td>0x01</td><td>Enable</td></tr></table>

Possible feature specific error status codes 

None 

# 3.8.25 Set Wind Noise Detection State command

This command allows the device to turn Wind Noise Reduction feature state On or Off. 


Table 3-199 Set Wind Noise Detection State command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>24</td><td>2</td><td>21.4</td><td>QCC517x/307x</td></tr></table>


Table 3-200 Set Wind Noise Detection State command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td>0x00</td><td>Disable wind noise detection</td></tr><tr><td>0x01</td><td>Enable wind noise detection</td></tr></table>

# Response PDU contents

None 

Possible feature specific error status codes 

None 

# 3.8.26 State Change notification

The State Change notification is sent when the AC feature type state changes. 


Table 3-201 State Change notification


<table><tr><td>Notification</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1</td><td>21.1</td><td>All</td></tr></table>


Table 3-202 State Change notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td></td><td>Feature type</td></tr><tr><td>0x01</td><td>ANC</td></tr><tr><td rowspan="2">2</td><td>0x00</td><td>Disabled</td></tr><tr><td>0x01</td><td>Enabled</td></tr></table>

# 3.8.27 Mode Change notification

The Mode Change notification is sent when the AC mode is updated. 


Table 3-203 Mode Change notification


<table><tr><td>Notification</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>1</td><td>1</td><td>21.1</td><td>All</td></tr></table>


Table 3-204 Mode Change notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x01 to 0x7f</td><td>Current mode</td></tr><tr><td>2</td><td>0x01 to 0xff</td><td>Feature type: 
■ 0x01: Static ANC 
■ 0x02: Leakthrough ANC 
■ 0x03: Adaptive ANC 
■ 0x04 to 0xff: Reserved</td></tr><tr><td>3</td><td>0x00 to 0x01</td><td>Adaptation control support: 
■ 0x00: Not supported 
■ 0x01: Supported</td></tr><tr><td>4</td><td>0x00 to 0x01</td><td>Gain control support: 
■ 0x00: Not supported 
■ 0x01: Supported</td></tr></table>

# 3.8.28 Gain Change notification

The Gain Change notification indicates the currently configured AC modes the user can toggle using the button/touch sensor of the device. 


Table 3-205 Gain Change notification


<table><tr><td>Notification</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>2</td><td>1</td><td>21.1</td><td>All</td></tr></table>


Table 3-206 Gain Change notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x01 to 0x7f</td><td>Current mode</td></tr><tr><td>2</td><td>0x01 to 0xff</td><td>Feature type: 
■ 0x01: Static ANC 
■ 0x02: Leakthrough ANC 
■ 0x03: Adaptive ANC 
■ 0x04 to 0xff: Reserved</td></tr><tr><td>3</td><td>0x00 to 0xff</td><td>Left gain value</td></tr><tr><td>4</td><td>0x00 to 0xff</td><td>Right gain value</td></tr></table>

# 3.8.29 Toggle Configuration notification

The Toggle Configuration notification indicates the currently configured AC modes the user can toggle using the button/touch sensor of the device. 


Table 3-207 Toggle Configuration notification


<table><tr><td>Notification</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>3</td><td>1</td><td>21.1</td><td>All</td></tr></table>


Table 3-208 Toggle Configuration notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x01 to 0x7f</td><td>Toggle Configuration number</td></tr><tr><td rowspan="3">2</td><td>0x00</td><td>Off</td></tr><tr><td>0x01 to 0x7f</td><td>AC mode</td></tr><tr><td>0xff</td><td>Void/ Toggle option not configured</td></tr></table>

# 3.8.30 Scenario Configuration notification

The Scenario Configuration notification indicates the AC mode behavior when in different scenarios, like idle state, voice calls or digital assistant use. 


Table 3-209 Scenario Configuration notification


<table><tr><td>Notification</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>4</td><td>1</td><td>21.1</td><td>All</td></tr></table>


Table 3-210 Scenario Configuration notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x01 to 0x7f</td><td>Scenario: 
■ 0x01: Idle 
■ 0x02: Playback/Music 
■ 0x03: Voice call 
■ 0x04: Digital assistant 
■ 0x05: Reserved</td></tr><tr><td>2</td><td>0x00</td><td>Toggle behavior: 
■ 0x00: Off 
■ 0x01 to 0x7f: Configured mode 
■ 0x80 to 0xFE: Reserved 
■ 0xff: Same as current</td></tr></table>

# 3.8.31 Demo State notification

The Demo State notification indicates if the device is in or out of Demo mode. 


Table 3-211 Demo State notification


<table><tr><td>Notification</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>5</td><td>1</td><td>21.1</td><td>RDP</td></tr></table>


Table 3-212 Demo State notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td>0x00</td><td>Out of Demo mode</td></tr><tr><td>0x01</td><td>In Demo mode</td></tr></table>

# 3.8.32 Adaptation Status Change notification

The Adaptation Status Change notification is sent if the Adaptation Status is paused or resumed. 


Table 3-213 Adaptation Status Change notification


<table><tr><td>Notification</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>6</td><td>1</td><td>21.1</td><td>All</td></tr></table>

NOTE (1) Applicable in Demo mode. 

(2) Applicable to Adaptive ANC modes only. 


Table 3-214 Adaptation Status Change notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td>0x00</td><td>Paused</td></tr><tr><td>0x01</td><td>Resumed</td></tr></table>

# 3.8.33 Leakthrough dB Gain Slider Configuration notification

This notification indicates the slider configuration supported by device for current mode. The notification provides the number of steps in which leakthrough dB gain can be configured, dB value of each step, minimum dB gain that can be configured and the step corresponding to current leakthrough dB gain. 


Table 3-215 Leakthrough dB Gain Slider Configuration notification


<table><tr><td>Notification</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>7</td><td>2</td><td>21.4</td><td>All</td></tr></table>


Table 3-216 Leakthrough dB Gain Slider Configuration notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td colspan="3">Comments</td></tr><tr><td>1</td><td>0x01 to 0x7F</td><td colspan="3">Current mode</td></tr><tr><td>2</td><td>0x03 to 0x0A</td><td colspan="3">Number of steps that are supported</td></tr><tr><td>3</td><td>0x02 to 0x05</td><td colspan="3">dB step size in which leakthrough gain can be updated</td></tr><tr><td>4</td><td>0x00 to 0xFF</td><td colspan="3">Minimum leakthrough dB gain supported by device</td></tr><tr><td>Payload byte</td><td>Value</td><td colspan="3">Comments</td></tr><tr><td rowspan="8"></td><td rowspan="8"></td><td colspan="3">Payload represents an 8-bit signed representation of minimum leakthrough dB gain (-128 dB to 127 dB)</td></tr><tr><td>Payload (bin)</td><td>Payload (hex)</td><td>dB gain</td></tr><tr><td>0000 0000</td><td>0x00</td><td>0</td></tr><tr><td>0000 1010</td><td>0x0A</td><td>10</td></tr><tr><td>0111 1111</td><td>0x7F</td><td>127</td></tr><tr><td>1000 0000</td><td>0x80</td><td>-128</td></tr><tr><td>1111 0110</td><td>0xF6</td><td>-10</td></tr><tr><td>1111 1111</td><td>0xFF</td><td>-1</td></tr><tr><td>5</td><td>0x01 to 0x0A</td><td colspan="3">Current step at which leakthrough slider points to</td></tr></table>

NOTE 1. This notification is only applicable for static/adaptive leakthrough mode. 

2. Adaptive leakthrough mode is not supported on QCC514x, QCC515x, QCC304x, QCC305x devices. 

3. This configuration may differ for each leakthrough mode. 

4. Leakthrough dB gain can be obtained from current step using formula: MinGain $^ +$ ( (CurStep - 1) * StepSize) 

# 3.8.34 Leakthrough dB Gain Change notification

The Leakthrough dB Gain Change notification is sent when leakthrough gain is updated and indicates the current step corresponding to leakthrough dB gain with which device is configured. 


Table 3-217 Leakthrough dB Gain Change notification


<table><tr><td>Notification</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>8</td><td>2</td><td>21.4</td><td>All</td></tr></table>


Table 3-218 Leakthrough dB Gain Change notification


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x01 to 0x7f</td><td>Current mode</td></tr><tr><td>2</td><td>0x01 to 0xA</td><td>Current step at which leakthrough slider should point to</td></tr></table>

NOTE 1. This notification is only applicable for static/adaptive leakthrough mode. 

2. Adaptive leakthrough mode is not supported on QCC514x, QCC515x, QCC304x, QCC305x devices. 

3. Leakthrough dB gain can be obtained from current step using formula: MinGain + ( (CurStep - 1) * StepSize) 

# 3.8.35 Left Right Balance notification

This notification is sent when Left Right Balance is updated and indicates the current balance with which device is configured. This is only applicable to modes where gain change is allowed by the user, for example, Static/Adaptive Leakthrough ANC modes. This balance will remain constant across all leakthrough modes. 


Table 3-219 Left Right Balance notification


<table><tr><td>Notification</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>9</td><td>2</td><td>21.4</td><td>All</td></tr></table>


Table 3-220 Left Right Balance notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td>0x00</td><td>Left</td></tr><tr><td>0x01</td><td>Right</td></tr><tr><td>2</td><td>0x00 to 0x064</td><td>Gain</td></tr></table>

NOTE The gain values will be in the range of 0 to 100 defining the percentage of balance. 

# 3.8.36 Wind Noise Detection State change notification

This notification indicates the Wind Noise Reduction feature state of both earbuds. 


Table 3-221 Wind Noise Detection State change notification


<table><tr><td>Notification</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>10</td><td>2</td><td>21.4</td><td>QCC517x/307x</td></tr></table>


Table 3-222 Wind Noise Detection State change notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td>0x00</td><td>Disabled</td></tr><tr><td>0x01</td><td>Enabled</td></tr></table>

# 3.8.37 Wind Noise Reduction Indication notification

This notification indicates the detection of wind noise on the left and right earbuds. 


Table 3-223 Wind Noise Reduction Indication notification


<table><tr><td>Notification</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>11</td><td>2</td><td>21.4</td><td>QCC517x/307x</td></tr></table>


Table 3-224 Wind Noise Reduction Indication notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td>0x00</td><td>Left: Wind not detected</td></tr><tr><td>0x01</td><td>Left: Wind detected</td></tr><tr><td rowspan="2">2</td><td>0x00</td><td>Right: Wind not detected</td></tr><tr><td>0x01</td><td>Right: Wind detected</td></tr></table>

# 3.9 Fit Status feature


Table 3-225 Fit Status


<table><tr><td>Feature ID</td><td>Version</td><td>ADK release</td></tr><tr><td>0x09</td><td>1</td><td>21.1</td></tr></table>

# 3.9.1 Set Fit Status command

The Set Fit Status command allows the device to perform the fit/wear check test. 


Table 3-226 Set Fit Status command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1</td><td>21.1</td><td>All</td></tr></table>


Table 3-227 Set Mode command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="2">1</td><td>0x00</td><td>Stop Fit Status test</td></tr><tr><td>0x01</td><td>Start Fit Status test</td></tr></table>

Response PDU contents 

None 

Possible Feature-specific Error status codes 

None 

# 3.9.2 Fit Status Indication notification

The Fit Status Indication notification indicates the fit status of the left and right earbud. 


Table 3-228 Fit Status Indication notification


<table><tr><td>Notification</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1</td><td>21.1</td><td>All</td></tr></table>


Table 3-229 Fit Status Indication notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0x01 to 0x03</td><td>Left fit result: 
■ 0x01: Good fit 
■ 0x02: Bad fit 
■ 0x03: Fail</td></tr><tr><td>2</td><td>0x01 to 0x03</td><td>Right fit result: 
■ 0x01: Good fit 
■ 0x02: Bad fit 
■ 0x03: Fail</td></tr><tr><td colspan="3">NOTE Good fit: The earbud can be considered as fitting well.
Bad fit: The earbud is not fitting well. The earbud may need adjusting or a change of ear tip may be required.
Fail: The fit test has failed. It could be due to various reasons, for example, user explicitly stopping the test or the test being interrupted due to a voice call or retesting needed in a quiet environment and so forth.</td></tr></table>

# 3.10 Voice Processing feature

The Voice Processing feature is intended to demo the different Qualcomm® CVCTM features using the Qualcomm CVC 3Mic capability. The Voice Processing GAIA feature is disabled by default. To enable this feature, add into the project definition: #define INCLUDE_CVC_DEMO 

# 3.10.1 Get Supported Enhancements command

The Gaia Client uses the Get Supported Enhancements command to decide whether the user can interact with the Voice Processing. 

The response contains a list of capabilities that are available on the device. 


Table 3-230 Get Supported Enhancements command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1</td><td>21.3</td><td>All</td></tr></table>


Table 3-231 Get Supported Enhancements command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>(any)</td><td>List offset: 
Transfer capabilities starting with this offset</td></tr></table>


Table 3-232 Get Supported Enhancements command response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 or 1</td><td>■ 0: No more data
■ 1: More data available</td></tr><tr><td>2</td><td>0 or 1</td><td>1st enhancement:
■ 0: CAPABILITY_NONE
■ 1: CAPABILITY_CVC_3MIC</td></tr></table>

Possible feature specific error status codes 

None 

# 3.10.2 Set Config Enhancement command

The Set Config Enhancement command sets a mode for a specific capability. 


Table 3-233 Set Config Enhancement command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>1</td><td>1</td><td>21.3</td><td>All</td></tr></table>


Table 3-234 Set Config Enhancement command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>1</td><td>Select capability
■ 1: CAPABILITY_CVC_3MIC</td></tr><tr><td>2</td><td>0 ~ 3</td><td>Microphone mode:
■ 0: Bypass mode
■ 1: cVc 1Mic mode
■ 2: cVc 2Mic mode
■ 3: cVc 3Mic mode</td></tr><tr><td>3</td><td>0 ~ 2</td><td>Bypass mode:
■ 0: Bypass voice microphone
■ 1: Bypass external microphone
■ 2: Bypass internal microphone</td></tr></table>

Response PDU contents 

None 


Table 3-235 Set Config Enhancement command possible feature error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Invalid parameter</td><td>0x05</td><td>The parameter was invalid</td></tr></table>

# 3.10.3 Get Config Enhancement command

The Get Config Enhancement command reads out a mode for a specific capability. 


Table 3-236 Get Config Enhancement command


<table><tr><td>Command ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>2</td><td>1</td><td>21.3</td><td>All</td></tr></table>


Table 3-237 Get Config Enhancement command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>1</td><td>Select capability (1 = CAPABILITY_CVC_3MIC)</td></tr></table>


Table 3-238 GetConfigEnhancement command response PDU contents for capability CVC_3MIC


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>1</td><td>Selected capability
■ 1: CAPABILITY_CVC_2MIC</td></tr><tr><td>2</td><td>0 ~ 3</td><td>Microphone mode:
■ 0: Bypass mode
■ 1: cVc 1Mic mode
■ 2: cVc 2Mic mode
■ 3: cVc 3Mic mode</td></tr><tr><td>3</td><td>0 ~ 2</td><td>Bypass mode:
■ 0: Bypass voice microphone
■ 1: Bypass external microphone
■ 2: Bypass internal microphone</td></tr><tr><td>4</td><td>0 ~ 1</td><td>Operation mode:
■ 0: 2Mic mode
■ 1: 3Mic mode
3Mic cVc capability operates in either 2-mic or 3-mic mode depending on the wind or noisy environment.</td></tr></table>


Table 3-239 Get Config Enhancement command possible feature specific error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Invalid parameter</td><td>0x05</td><td>The command was valid, but the device has no valid settings.</td></tr><tr><td>Incorrect state</td><td>0x06</td><td>The device is not in the correct state to process the command.</td></tr></table>

# 3.10.4 Notify Enhancement Mode Change notification

The Notify Enhancement Mode Change notification informs the GAIA app about changes in a specific capability. 

The capability is defined in the first payload byte. The following values are specific to this capability. 


Table 3-240 Notify Enhancement Mode Change notification


<table><tr><td>Notification ID</td><td>Feature version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1</td><td>21.3</td><td>All</td></tr></table>


Table 3-241 Notify Enhancement Mode Change notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>1</td><td>Selected capability
■ 1: CAPABILITY_CVC_3MIC</td></tr><tr><td>2</td><td>0 ~ 3</td><td>Microphone mode:
■ 0: Bypass mode
■ 1: cVc 1Mic mode
■ 2: cVc 2Mic mode
■ 3: cVc 3Mic mode</td></tr><tr><td>3</td><td>0 ~ 2</td><td>Bypass mode
■ 0: Bypass voice microphone
■ 1: Bypass external microphone
■ 2: Bypass internal microphone</td></tr><tr><td>4</td><td>0 ~ 1</td><td>Operation mode:
■ 0: 2Mic mode
■ 1: 3Mic mode
3Mic cVc capability operates in either 2-mic or 3-mic mode depending on the wind or noisy environment.</td></tr></table>

Possible feature specific error status codes 

None 

# 3.11 UI Gesture Configuration feature

This feature allows the end user to configure what their Earbuds do when they perform gestures such as tap, swipe, long press and so on on the device touchpad sensor. It also allows differentiation of the gestures on the Left and Right Earbuds. For example, the end user may wish 'volume up' to be performed by a tap of the left Earbud and 'volume down' by a tap of the right Earbud. 

The end user reconfigures the ROM default behavior using the 'Gesture configuration' screens of the iOS or Android GAIA Application. Their chosen settings are applied immediately when entered through the GAIA App GUI and stored in NVRAM on the devices. They persist through a DFU session. The list of configurable gestures, contexts and actions are designed to be extensible in future ADKs. Any 

version of the GAIA App including this feature shall be compatible with any CAA Application that includes the feature. 

NOTE This feature can be used with any CAA Application, not just Earbuds. 

These commands and notifications are provided by the UI Configuration GAIA plug-in. 


Table 3-242 UI Gesture Configuration


<table><tr><td>Feature ID</td><td>Version</td><td>ADK release</td></tr><tr><td>0x0b</td><td>1</td><td>21.3</td></tr></table>

# 3.11.1 Get Number of Touchpads command

The Get Number of Touchpads command is sent by the mobile application in order to discover the number of originating touchpads that the embedded device has available for configuration. This query has no message payload. 


Table 3-243 Get Number of Touchpads command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1+</td><td>21.4</td><td>All</td></tr></table>

# Command parameters

None 


Table 3-244 Get Number of Touchpads command response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td rowspan="4">1</td><td rowspan="4">1 or 2</td><td>Value Comment</td></tr><tr><td>0 Reserved</td></tr><tr><td>1 Denotes a device with a single originating touchpad.All Configuration Query/Set commands with this device have theoriginating touchpad value 0 == &#x27;single&#x27;.</td></tr><tr><td>2 Denotes a device in an Earbud pair or possibly a Headsetapplication, where there are discrete Left and Right touchpads.Configuration Query/Set commands with this device have originatingsupportive touchpad values of either 1 == &#x27;right&#x27;, 2 == &#x27;left&#x27;, or 3 == &#x27;both&#x27;</td></tr></table>

# Possible feature specific error status codes

None 

# 3.11.2 Get Supported Gestures Command

The Get Supported Gestures command is sent by the mobile application in order to discover the Gesture IDs supported by the embedded device. This query has no message payload. 


Table 3-245 Get Supported Gestures command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>1</td><td>1+</td><td>21.3</td><td>All</td></tr></table>

# Command parameters

None 


Table 3-246 Get Supported Gestures command response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1 to 16</td><td></td><td>Bit Array of supported Gesture IDsGesture ID 0 is 1&lt;&lt;0, that is, 0x01Gesture ID 7 is 1&lt;&lt;7, that is, 0x80...Gesture ID 127 is 0x80 in the 16th byte</td></tr></table>

This is a multi-byte bitfield, where the bit index denotes the Gesture ID. All supported gestures are set to 1. All unsupported gestures are set to 0. 

# Possible Feature specific Error Status codes

None 

# 3.11.3 Get Supported Contexts command

The Get Supported Contexts command is sent by the mobile application in order to discover the Context IDs supported by the embedded device. This query has no message payload. 


Table 3-247 Get Supported Contexts command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>2</td><td>1+</td><td>21.4</td><td>All</td></tr></table>

Command parameters 

None 


Table 3-248 Get Supported Contexts command response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1 to 16</td><td></td><td>Bit Array of supported Context IDs
Context ID 0 is 1&lt;&lt;0, that is, 0x01
Context ID 7 is 1&lt;&lt;7, that is, 0x80
... 
Context ID 127 is 0x80 in the 16th byte</td></tr></table>

This is a multi-byte bitfield, where the bit index denotes the Context ID. All supported contexts are set to 1. All unsupported contexts are set to 0. 

Possible Feature specific Error Status codes 

None 

# 3.11.4 Get Supported Actions command

The Get Supported Actions command is sent by the mobile application in order to discover the Action IDs supported by the embedded device. This query has no message payload. 


Table 3-249 Get Supported Actions command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>3</td><td>1+</td><td>21.3</td><td>All</td></tr></table>

Command parameters 

None 


Table 3-250 Get Supported Actions command response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1 to 16</td><td></td><td>Bit Array of supported Action IDsAction ID 0 is 1&lt;&lt;0, that is, 0x01Action ID 7 is 1&lt;&lt;7, that is, 0x80…Action ID 127 is 0x80 in the 16th byte</td></tr></table>

This is a multi-byte bitfield, where the bit index denotes the Action Id. All supported actions are set to 1. All unsupported actions are set to 0. 

Possible Feature specific Error Status codes 

None 

# 3.11.5 Get Configuration For Gesture command

This query is sent from the Mobile application to the UI Configuration GAIA plug-in. It is sent when the Mobile app needs to populate its GUI with the current UI configuration active in the embedded device. 

The query comprises of two bytes: 

1. Firstly, a byte specifies the Gesture ID whose configuration is being queried by the Mobile application. 

2. Secondly, a byte specifies the offset for which to read the configuration data at. This is needed when the configuration data is indicated as exceeding the minimum 16-byte MTU (for some BLE transports) (this is signaled by the embedded app in its response to the first query for that gesture ID). 


Table 3-251 Get Configuration For Gesture command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>4</td><td>1+</td><td>21.4</td><td>All</td></tr></table>


Table 3-252 Get Configuration For Gesture command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td></td><td>The Gesture ID for which the Mobile application is querying the configuration.</td></tr><tr><td>2</td><td></td><td>The byte offset to read the configuration data from.</td></tr></table>


Table 3-253 Get Configuration For Gesture command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>Valid gesture ID</td><td>The Gesture ID for which the Mobile application is querying the configuration</td></tr><tr><td>2</td><td>Any</td><td>The byte offset to read the configuration from</td></tr></table>


Table 3-254 Get Configuration For Gesture command response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td colspan="12">Comments</td><td></td><td></td><td></td><td></td></tr><tr><td rowspan="4">1</td><td rowspan="4">Any</td><td colspan="12">Firstly, there is a single-byte bitfield specifying:</td><td></td><td></td><td></td><td></td></tr><tr><td>Bit7</td><td>Bit6</td><td>Bit5</td><td>Bit4</td><td>Bit3</td><td>Bit2</td><td>Bit1</td><td>Bit0</td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr><tr><td>More data</td><td colspan="7">Gesture ID the configuration is for</td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr><tr><td colspan="8">The more data flag is set to 1 if the embedded app has more data to send to the mobile app, that is, the payload needs to be fragmented over several packets. If this bit is 0, all the requested data is present in this response.</td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr><tr><td rowspan="3">2 and 3, 
4 and 5, 
and so on (up to maximum of 14 and 15)</td><td rowspan="3">Any</td><td colspan="8">Secondly, there is an array of an even number of bytes, where each byte pair forms a single 16 bit word. Therefore this is an array of 0 to 7 uint16s. Each uint16 represents a bitfield encoded as follows:</td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr><tr><td>Bi t1 5</td><td>Bi t1 4</td><td>Bi t1 3</td><td>Bi t1 2</td><td>Bi t1 1</td><td>Bi t1 0</td><td>Bi t1 9</td><td>Bi t8</td><td>Bi t7</td><td>Bi t6</td><td>Bi t5</td><td>Bi t4</td><td>Bi t3</td><td>Bi t2</td><td>Bi t1</td><td>Bi t0</td></tr><tr><td>Originating touchpad</td><td colspan="7">Configured Context ID</td><td colspan="8">Configured Action ID</td></tr></table>


Table 3-254 Get Configuration For Gesture command response PDU contents (cont.)


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td><td></td></tr><tr><td></td><td></td><td>The Origin Touchpad value denotes one of Single/Left/Right/Both and identifies the touchpad for which the gesture has this configured action and context. Single is used when there is only a single touchpad on the device.</td><td></td></tr><tr><td></td><td></td><td>Single</td><td>0</td></tr><tr><td></td><td></td><td>Right</td><td>1</td></tr><tr><td></td><td></td><td>Left</td><td>2</td></tr><tr><td></td><td></td><td>Both</td><td>3</td></tr><tr><td></td><td></td><td colspan="2">Bits 7 to 13 denote the context ID that should match in order to trigger the configured action. 
Bits 0 to 6 denote the configured action ID.</td></tr></table>

# Possible Feature specific Error Status codes

None 

# 3.11.6 Set Configuration For Gesture command

This command is sent whenever the user changes the actions associated with a gesture in the Mobile App GUI. It uses a similar format to the response for the Get Configuration For Gesture command (that is, the message used to populate the Mobile Application's GUI with the original configuration state data). The mobile application shall send one or more commands (it may be necessary to modify the configuration of two or more gestures due to the users change) as follows: 


Table 3-255 Set Configuration For Gesture command


<table><tr><td>Command ID</td><td>Version</td><td>ADK Release</td><td>Variants</td></tr><tr><td>5</td><td>1+</td><td>21.4</td><td>All</td></tr></table>


Table 3-256 Set Configuration For Gesture command parameters


<table><tr><td>Payload byte</td><td>Value</td><td colspan="8">Comments</td></tr><tr><td rowspan="4">1</td><td rowspan="4">Any</td><td colspan="8">Firstly, there shall be a single-byte bitfield specifying:</td></tr><tr><td>Bit7</td><td>Bit6</td><td>Bit5</td><td>Bit4</td><td>Bit3</td><td>Bit2</td><td>Bit1</td><td>Bit0</td></tr><tr><td>More Data</td><td colspan="7">Gesture ID for which the configuration is being set</td></tr><tr><td colspan="8">The More Data flag is set to 1 if the mobile app has more data to send to the plug-in, that is, the payload needs to be fragmented over several packets. If this bit is 0, all the data is presented in this command.</td></tr><tr><td>2</td><td></td><td colspan="8">Secondly, there is a byte specifying the index to write at (this is always 0 for the first packet of the Set command, and is a non-zero only when continuing fragmented SET commands which were spread over several packets).</td></tr></table>


NOTE As 7 records (14 bytes) fit into a packet, the first continuation packet writes at index 7. 



When the mobile App sends a new command (that is, to configure the next Gesture ID), then the index\once again starts at 0. 


<table><tr><td rowspan="9">3 and 4,
5 and 6, and so on</td><td>Any</td><td colspan="14">Thirdly, there is an array of an even number of bytes, where each byte pair forms a single 16-bit word. Therefore this is an array of 0 to 7 uint16s. Each uint16 represents a bitfield encoded as follows:</td><td></td></tr><tr><td>Bit15</td><td>Bit14</td><td>Bit13</td><td>Bit12</td><td>Bit11</td><td>Bit10</td><td>Bit9</td><td>Bit8</td><td>Bit7</td><td>Bit6</td><td>Bit5</td><td>Bit4</td><td>Bit3</td><td>Bit2</td><td>Bit1</td><td>Bit0</td></tr><tr><td colspan="2">Originating Touchpad</td><td colspan="7">Configured Context ID</td><td colspan="7">Configured Action ID</td></tr><tr><td colspan="16">The originating touchpad value denotes one of Single/Left/Right/Both and identifies the touchpad which can originate the gesture that has this configured action and context.</td></tr><tr><td colspan="10">Single</td><td colspan="6">0</td></tr><tr><td colspan="10">Right</td><td colspan="6">1</td></tr><tr><td colspan="10">Left</td><td colspan="6">2</td></tr><tr><td colspan="10">Both</td><td colspan="6">3</td></tr><tr><td colspan="16">Bits 7 to 13 denote the context ID that should match in order to trigger the configured action.
Bits 0 to 6 denote the configured action ID.</td></tr></table>

# Response PDU contents

None 

Possible Feature specific Error Status codes 

None 

# 3.11.7 Reset Configuration To Defaults command

This command is sent when the end user wishes to reset the embedded device(s) to their original default UI configuration from the GAIA application (that is, not via a factory reset of the entire device). 


Table 3-257 Reset Configuration To Defaults command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>6</td><td>1+</td><td>21.4</td><td>All</td></tr></table>

# Command Parameters

None 

# Response PDU contents

None 


Table 3-258 Reset Configuration To Defaults command possible feature specific error status codes


<table><tr><td>Status</td><td>Code</td><td>Description</td></tr><tr><td>Peer Link Down</td><td>0x80</td><td>The removal of the NVRAM-stored end-user configuration can only proceed when the peer link between the Earbud devices is up at the time of the &#x27;Reset Configuration to Defaults&#x27; command being received. If it is not up, the Primary Earbud will respond with this error code to the GAIA App. In that case the GAIA App presents a dialog to the user requesting them to ensure both Earbuds are out of case and ready for the configuration to be reset.</td></tr></table>

# 3.11.8 Gesture configuration Changed notification

The embedded application sends this notification to all connected mobile applications when a gesture configuration has been updated. This allows other connected applications, which did not instigate the change, to refresh the state presented to the user in their GUIs. 


Table 3-259 Gesture configuration Changed notification


<table><tr><td>Notification ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1</td><td>21.4</td><td>All</td></tr></table>


Table 3-260 Gesture configuration Changed notification parameters


<table><tr><td>Payload byte</td><td>Value</td><td colspan="8">Comments</td></tr><tr><td rowspan="3">1</td><td rowspan="3">Varie s</td><td colspan="8">There is a single-byte value specifying the 7-bit gesture ID that changes:</td></tr><tr><td>Bit7</td><td>Bit6</td><td>Bit5</td><td>Bit4</td><td>Bit3</td><td>Bit2</td><td>Bit1</td><td>Bit0</td></tr><tr><td>Reserve d</td><td colspan="7">Gesture ID for which the configuration was changed</td></tr></table>

# 3.11.9 Configuration Reset To Defaults notification

The embedded application sends this message to all connected mobile applications when the UI configuration has been reset to its ROM default values. It has no payload. 


Table 3-261 Configuration Reset to Defaults notification


<table><tr><td>Notification ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>1</td><td>1</td><td>21.4</td><td>All</td></tr></table>

Notification Parameters 

None 

# 3.12 Statistics reporting feature

This API has the notion of "Category IDs" and "Statistic IDs". 

■ A category ID refers to a grouping of related statistics - for example streaming statistics. Each grouping of statistics must have a unique category ID. To support the use of customer implemented extensions, category ID $0 \times 8 0 0 0$ and above are reserved for customer use. 

A statistic ID identifies an individual statistic within a group, for example bitrate within the streaming statistic group. 

To minimize BT traffic, when values are requested, more than one value may be requested within a single group. 


Table 3-262 Statistics reporting feature


<table><tr><td>Feature ID</td><td>Version</td><td>ADK Release</td></tr><tr><td>0x0c</td><td>1</td><td>22.1</td></tr></table>

# 3.12.1 Get Supported Categories Command

This command is sent by the mobile application to discover the category IDs supported by the embedded device. 


Table 3-263 Get Supported Categories command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>0</td><td>1+</td><td>22.1</td><td>All</td></tr></table>


Table 3-264 Get Supported Categories command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comment</td></tr><tr><td>1 - 2</td><td>0 - 65535</td><td>Last received category ID. For first request this value is 0.</td></tr></table>


Table 3-265 Get Supported Categories command response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 or 1</td><td>1 denotes there is more data to come after this response and a further request should be made passing the last received category id.</td></tr><tr><td>2 - 3</td><td>1 - 65535</td><td>Category ID. Category IDs are returned in numeric order, smallest first.</td></tr><tr><td>4 - 5</td><td>1 - 65535</td><td>Next category ID. List repeats until maximum allowed in response packet.</td></tr></table>

Possible Feature specific error status codes 

None. 

# 3.12.2 Get All Statistics for Category command

This command is sent by the mobile application to discover the statistic IDs supported by the embedded device. 


Table 3-266 Get All Statistics For Category command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>1</td><td>1+</td><td>22.1</td><td>All</td></tr></table>


Table 3-267 Get All Statistics For Category command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1 - 2</td><td>1 - 65535</td><td>Category ID</td></tr><tr><td>3</td><td>0 - 255</td><td>Last received Statistic ID. For first request this value is 0.</td></tr></table>


Table 3-268 Get All Statistics For Category command response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 or 1</td><td>1 denotes there is more data to come after this response and a further request should be made passing the last received statistic ID.</td></tr><tr><td>2 - 3</td><td>1 - 65535</td><td>Category ID</td></tr><tr><td>2 to max permitted in packet</td><td></td><td>List of statistics values in format described below (value entries do not include optional Category ID field for this command). Values are returned in numeric order of the statistic ID, smallest first.</td></tr></table>

Possible Feature specific error status codes 

None 

# 3.12.3 Get Statistics Values command

This command is sent by the mobile application to determine the values of a requested set of statistic IDs. 


Table 3-269 Get Statistics Values command


<table><tr><td>Command ID</td><td>Version</td><td>ADK release</td><td>Variants</td></tr><tr><td>2</td><td>1+</td><td>22.1</td><td>All</td></tr></table>


Table 3-270 Get Statistics Values command parameters


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1 to max permitted in packet</td><td></td><td>List of statistic ID requests. The list shall consist of entries of the format: 
Bytes 1-2: Category ID (1 – 65535) 
Byte 3: Statistic ID (1 – 255)</td></tr></table>


Table 3-271 Get Statistics Values command response PDU contents


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1</td><td>0 or 1</td><td>Denotes whether the entire request was fulfilled. Unlike other requests this command does not support “continuation” requests. A new request passing a new list of command IDs should be made for the unfulfilled values. Values are returned in numeric order of the statistic ID, smallest first.</td></tr><tr><td>2 to max permitted in packet or required to fulfil the request.</td><td></td><td>List of statistics values in format described below</td></tr></table>


Table 3-272 Statistic value entry


<table><tr><td>Payload byte</td><td>Value</td><td>Comments</td></tr><tr><td>1 - 2</td><td>1 - 65535</td><td>Category ID (Optional, only present in responses to Get Statistics Values command)</td></tr><tr><td>3</td><td>1 - 255</td><td>Statistic ID</td></tr><tr><td>4</td><td>0 - 255</td><td>Flags (TBC but to include suitability for regular refresh)</td></tr><tr><td>5</td><td>0 - 255</td><td>Length of value (0 if not available)</td></tr><tr><td>6+</td><td></td><td>Value as described by the type above (1, 2 or 4 bytes). If the value is not available then there is no value and length is 0</td></tr></table>

<table><tr><td>Document</td><td>Reference</td></tr><tr><td>Using the Debug Partition Application Note</td><td>80-CH505-1 / CS-00420649-AN</td></tr><tr><td>Term</td><td>Definition</td></tr><tr><td>ANC</td><td>Active Noise Cancellation</td></tr><tr><td>API</td><td>Application Programming Interface</td></tr><tr><td>Bluetooth SIG</td><td>Bluetooth Special Interest Group</td></tr><tr><td>DFU</td><td>Device Firmware Upgrade</td></tr><tr><td>DLE</td><td>Data Length Extension</td></tr><tr><td>GAIA</td><td>Generic Application Interface Architecture</td></tr><tr><td>GATT</td><td>Generic Attributes</td></tr><tr><td>iAP</td><td>iPod Accessory Protocol</td></tr><tr><td>LE</td><td>Low Energy</td></tr><tr><td>PDU</td><td>Protocol Data Unit</td></tr><tr><td>QTIL</td><td>Qualcomm Technologies International, Ltd.</td></tr><tr><td>RFCOMM</td><td>Radio Frequency COMMunication</td></tr><tr><td>SOF</td><td>Start of Frame</td></tr><tr><td>VA</td><td>Voice Assistant</td></tr></table>