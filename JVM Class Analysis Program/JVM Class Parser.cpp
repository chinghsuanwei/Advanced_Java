#include <iostream>
#include <iomanip>
#include <sstream>
#include <fstream>
#include <string>
#include <map>

using namespace std;

const char HEXVAL[16] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
const int HEXPOWER[4] = {1, 256, 65535, 16777216};

unsigned int howManyDigits( int iNumber )
{
	stringstream ss;
	string sNumber;
	ss << iNumber;
	ss >> sNumber;
	return sNumber.length();
}

string transformByteCodeToHexString( char* byteCode, int nSize )
{
    string sHex;
    for( int i=0; i<nSize; i++ ){
        sHex.append( 1, HEXVAL[ ( byteCode[i] >> 4 ) & 0x0f ] );
        sHex.append( 1, HEXVAL[ byteCode[i] & 0x0f  ] );
    }
    return sHex;
}

unsigned int transformByteCodeToOct( char* byteCode, int nSize )
{
    unsigned int iOct = 0;
    for( int i=0; i<nSize; i++ ){
        iOct += HEXPOWER[(nSize-i-1)] * (unsigned char)byteCode[i] ;
    }
	return iOct;
}

void constantPoolFunctionUTF8( char* buf, ifstream& inFile, ofstream& outFile)
{
	inFile.read( buf, 2 );
	unsigned int nSize = transformByteCodeToOct( buf, 2 );
	inFile.read( buf, nSize );
	buf[ nSize ] = '\0';
	outFile << "UTF8(" << nSize << ", \""  << buf << "\")" << endl;
}

void constantPoolFunctionClassInfo( char* buf, ifstream& inFile, ofstream& outFile )
{
	inFile.read( buf, 2 );
	unsigned int iClassIndex = transformByteCodeToOct( buf, 2 );
	outFile << "Class(" << iClassIndex << ")" << endl;
}

void constantPoolFunctionStringInfo( char* buf, ifstream& inFile, ofstream& outFile )
{
	inFile.read( buf, 2 );
	unsigned int iStringIndex = transformByteCodeToOct( buf, 2 );
	outFile << "String(" << iStringIndex << ")" << endl;
}

void constantPoolFunctionFieldRef( char* buf, ifstream& inFile, ofstream& outFile )
{
	//FieldRef(class: 16, 17)
	inFile.read( buf, 2 );
	unsigned int param1 = transformByteCodeToOct( buf, 2 );
	inFile.read( buf, 2 );
	unsigned int param2 = transformByteCodeToOct( buf, 2 );
	outFile << "FieldRef(class: " << param1 << ", " << param2 <<   ")" << endl;
}

void constantPoolFunctionMethodRef( char* buf, ifstream& inFile, ofstream& outFile )
{
	//Methodref(class: 6, 15)
	inFile.read( buf, 2 );
	unsigned int param1 = transformByteCodeToOct( buf, 2 );
	inFile.read( buf, 2 );
	unsigned int param2 = transformByteCodeToOct( buf, 2 );
	outFile << "MethodRef(class: " << param1 << ", " << param2 <<   ")" << endl;
}

void constantPoolFuntionNameAndTypeInfo( char* buf, ifstream& inFile, ofstream& outFile )
{
	//NameAndType(24, 25)
	inFile.read( buf, 2 );
	unsigned int param1 = transformByteCodeToOct( buf, 2 );
	inFile.read( buf, 2 );
	unsigned int param2 = transformByteCodeToOct( buf, 2 );
	outFile << "NameAndType(" << param1 << ", " << param2 <<   ")" << endl;
}

int main()
{
    char buf[65535];
    string sInFileName;
    string sOutFileName;
	cout << "Please Enter Java class file name: ";
    cin >> sInFileName;

    ifstream inFile;
    ofstream outFile;
    inFile.open(sInFileName.c_str(), ifstream::in | ifstream::binary );
	if( inFile.fail() ){
		cout << "Error: Cannot find file \"" << sInFileName << "\"" << endl;
		system("pause");
		exit(0);
	}
    
    int found = sInFileName.rfind('.');
    sOutFileName = sInFileName.substr(0, found);
    sOutFileName.append(".jvm");
    cout << "Output File Name:" << sOutFileName << endl;
    outFile.open(sOutFileName.c_str(), ofstream::out);
    
    inFile.read( buf, 4 );
    outFile << "u4 magic;\t" << transformByteCodeToHexString( buf, 4 ) << endl;
    inFile.read( buf, 2 );
    outFile << "u2 major_version;\t" << transformByteCodeToHexString( buf, 2 ) << endl;
    inFile.read( buf, 2 );
    outFile << "u2 minor_version;\t" << transformByteCodeToHexString( buf, 2 ) << endl;    
    inFile.read( buf, 2 );
	unsigned int constant_pool_count = transformByteCodeToOct( buf, 2 );
    outFile << "u2 constant_pool_count;\t" << transformByteCodeToHexString( buf, 2 ) << "(" << constant_pool_count << ")" << endl;      
    
	outFile << endl  << endl;
	outFile << "contant_pool:" << endl;
	typedef void (* ConstantPoolFunction)(char* , ifstream&, ofstream&);
	map< int, ConstantPoolFunction > mConstantPoolFunction;
	
	mConstantPoolFunction[1] = &constantPoolFunctionUTF8;
	mConstantPoolFunction[7] = &constantPoolFunctionClassInfo;
	mConstantPoolFunction[8] = &constantPoolFunctionStringInfo;
	mConstantPoolFunction[9] = &constantPoolFunctionFieldRef;
	mConstantPoolFunction[10] = &constantPoolFunctionMethodRef;
	mConstantPoolFunction[12] = &constantPoolFuntionNameAndTypeInfo;
	
	unsigned int nDigit = howManyDigits( constant_pool_count-1 );
	for( unsigned int i=1; i<constant_pool_count; i++ ){
		outFile << setw(nDigit) << setfill('0') << i << ":";
		inFile.read( buf, 1 );
		unsigned int iTag = transformByteCodeToOct( buf, 1 );
		outFile << setw(3) << setfill(' ') << iTag << ": ";
		(*mConstantPoolFunction[iTag])( buf, inFile, outFile );
	}
	
	outFile << endl;
	
	inFile.read( buf, 2 );
	outFile << "u2 access_flags;\t" << transformByteCodeToHexString( buf, 2 ) << endl;
	inFile.read( buf, 2 );
	outFile << "u2 this_class;\t" << transformByteCodeToHexString( buf, 2 ) << endl;
	inFile.read( buf, 2 );
	outFile << "u2 super_class;\t" << transformByteCodeToHexString( buf, 2 ) << endl;
	inFile.read( buf, 2 );
	unsigned int interfaces_count = transformByteCodeToOct( buf, 2 );
	outFile << "u2 interfaces_count;\t" << transformByteCodeToHexString( buf, 2 ) << "(" << interfaces_count << ")" << endl;
	
	outFile << "interfaces[interfaces_count]" << endl;
	if( interfaces_count > 0 ){
		cout << "Error: Cannot handle interface" << endl;
		system("pause");
		exit(0);
	}
	for( unsigned int i=1; i<=interfaces_count; i++){
		//TODO
	}
	
	inFile.read( buf, 2 );
	unsigned int fields_count = transformByteCodeToOct( buf, 2 );
	outFile << "u2 fields_count;\t" << transformByteCodeToHexString( buf, 2 ) << "(" << fields_count << ")" << endl;
	outFile << "field_info fields[fields_count];" << endl;
	for( unsigned int i=1; i<=fields_count; i++){
		outFile << "\tField_info{" << endl;
		inFile.read( buf, 2 );
		outFile << "\t\tu2 access_flags;\t" << transformByteCodeToHexString( buf, 2 ) << endl;
		inFile.read( buf, 2 );
		outFile << "\t\tu2 name_index;\t" << transformByteCodeToHexString( buf, 2 ) << endl;
		inFile.read( buf, 2 );
		outFile << "\t\tu2 descriptor_index;\t" << transformByteCodeToHexString( buf, 2 ) << endl;
		inFile.read( buf, 2 );
		unsigned int attributes_count = transformByteCodeToOct( buf, 2 );
		outFile << "\t\tu2 attributes_count;\t" << transformByteCodeToHexString( buf, 2 ) << "(" << attributes_count << ")" << endl;
		outFile << "\t\tattribute_info attributes[attributes_count];" << endl;
		for( int j=1; j<=attributes_count; j++ ){
			inFile.read( buf, 2 );
			outFile << "\t\t\tu2 attribute_name_index;\t" << transformByteCodeToHexString( buf, 2 ) << endl;
			inFile.read( buf, 4 );
			unsigned int attribute_length = transformByteCodeToOct( buf, 4 );
			outFile << "\t\t\tu4 attribute_length;\t" << transformByteCodeToHexString( buf, 4 ) << "(" << attribute_length << ")" << endl;
			outFile << "\t\t\tu1 info[attribute_length];" << endl;
			inFile.read( buf, attribute_length );
			outFile << "\t\t\t\t" << transformByteCodeToHexString( buf, attribute_length ) << endl;
		}
		outFile << "\t}" << endl;
	}
	outFile << endl;
	
	inFile.read( buf, 2 );
	unsigned int methods_count = transformByteCodeToOct( buf, 2 );	
	outFile << "u2 methods_count;\t" << transformByteCodeToHexString( buf, 2 ) << "(" << methods_count << ")" << endl;
	outFile << endl;
	for( unsigned int i=1; i<=methods_count; i++ ){
		outFile << "Method[" << i << "]" << endl << endl;
		inFile.read( buf, 2 );
		outFile << "u2 access_flags;\t" << transformByteCodeToHexString( buf, 2 ) << endl;
		inFile.read( buf, 2 );
		outFile << "u2 name_index;\t" << transformByteCodeToHexString( buf, 2 ) << endl;
		inFile.read( buf, 2 );
		outFile << "u2 descriptor_index;\t" << transformByteCodeToHexString( buf, 2 ) << endl;
		inFile.read( buf, 2 );
		unsigned int attributes_count = transformByteCodeToOct( buf, 2 );
		outFile << "u2 attributes_count;\t" << transformByteCodeToHexString( buf, 2 ) << "(" << attributes_count << ")" << endl;
		outFile << "attribute_info attributes[attributes_count];" << endl;
		for( int j=1; j<=attributes_count; j++ ){
			inFile.read( buf, 2 );
			outFile << "\tu2 attribute_name_index;\t" << transformByteCodeToHexString( buf, 2 ) << endl;
			inFile.read( buf, 4 );
			unsigned int attribute_length = transformByteCodeToOct( buf, 4 );
			outFile << "\tu4 attribute_length;\t" << transformByteCodeToHexString( buf, 4 ) << "(" << attribute_length << ")" << endl;
			outFile << "\tu1 info[attribute_length];" << endl;
			inFile.read( buf, attribute_length );
			outFile << "\t\t" << transformByteCodeToHexString( buf, attribute_length ) << endl;
			outFile << endl;
		}
	}
	
	inFile.read( buf, 2 );
	unsigned int Attributes_count = transformByteCodeToOct( buf, 2 );
	outFile << "Attributes count;\t" << transformByteCodeToHexString( buf, 2 ) << "(" << Attributes_count << ")" << endl;

	for( unsigned int i=1; i<=Attributes_count; i++ ){
		outFile << "\tSourceFile_attribute {" << endl;
		inFile.read( buf, 2 );
		outFile << "\t\tu2 attribute_name_index;\t" << transformByteCodeToHexString( buf, 2 ) << endl;
		inFile.read( buf, 4 );
		unsigned int attribte_length = transformByteCodeToOct( buf, 4 );
		outFile << "\t\tu4 attribute_length;\t" << transformByteCodeToHexString( buf, 4 ) << "(" << attribte_length  << ")" << endl;
		inFile.read( buf, 2 );
		outFile << "\t\tu2 sourcefile_index;\t" << transformByteCodeToHexString( buf, 2 ) << endl;
		outFile << "\t}" << endl;
	}
	
	//check if file stream is all read
	int iCurLength = inFile.tellg();
	inFile.seekg( 0, ios::end );
	int iTotalLength = inFile.tellg();
	cout << iCurLength << " / " << iTotalLength << endl;
	
	inFile.close();
    outFile.close();
    system("pause");
}

