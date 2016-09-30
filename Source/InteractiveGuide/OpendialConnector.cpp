// Fill out your copyright notice in the Description page of Project Settings.

#include "InteractiveGuide.h"
#include "Networking.h"
#include "OpendialConnector.h"
#include<string>


// Sets default values for this component's properties
UOpendialConnector::UOpendialConnector()
{
	// Set this component to be initialized when the game starts, and to be ticked every frame.  You can turn these features
	// off to improve performance if you don't need them.
	bWantsBeginPlay = true;
	PrimaryComponentTick.bCanEverTick = true;

	// ...
}


// Called when the game starts
void UOpendialConnector::BeginPlay()
{
	Super::BeginPlay();

	OpendialSocket = ISocketSubsystem::Get(PLATFORM_SOCKETSUBSYSTEM)->CreateSocket(NAME_Stream, TEXT("default"), false);

	int32 port = 5531;
	bool isValid = true;

	TSharedRef<FInternetAddr> addr = ISocketSubsystem::Get(PLATFORM_SOCKETSUBSYSTEM)->CreateInternetAddr();
	addr->SetIp(TEXT("127.0.0.1"), isValid);
	addr->SetPort(port);

	bool connected = OpendialSocket->Connect(*addr);
}


// Called every frame
void UOpendialConnector::TickComponent(float DeltaTime, ELevelTick TickType, FActorComponentTickFunction* ThisTickFunction)
{
	Super::TickComponent(DeltaTime, TickType, ThisTickFunction);

	uint32 Size;
	TArray<uint8> ReceivedData;
	while (OpendialSocket->HasPendingData(Size))
	{
		ReceivedData.SetNumUninitialized(FMath::Min(Size, 65507u));

		int32 Read = 0;
		OpendialSocket->Recv(ReceivedData.GetData(), ReceivedData.Num(), Read);

	}

	if (ReceivedData.Num() <= 0)
	{
		//No Data Received
		return;
	}

	GEngine->AddOnScreenDebugMessage(-1, 5.f, FColor::Red, FString::Printf(TEXT("Data Bytes Read ~> %d"), ReceivedData.Num()));
	Message = StringFromBinaryArray(ReceivedData);
}

FString UOpendialConnector::StringFromBinaryArray(const TArray<uint8>& BinaryArray)
{
	//Create a string from a byte array!
	const std::string cstr(reinterpret_cast<const char*>(BinaryArray.GetData()), BinaryArray.Num());

	//FString can take in the c_str() of a std::string
	return FString(cstr.c_str());
}

