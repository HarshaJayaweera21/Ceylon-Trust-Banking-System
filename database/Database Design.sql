USE BankingSystemDB;  -- Use your existing database
GO


-- 1. Roles Table: Defines user roles for RBAC
CREATE TABLE Roles (
    RoleID INT PRIMARY KEY IDENTITY(1,1),
    RoleName VARCHAR(50) UNIQUE NOT NULL,  -- e.g., 'Customer', 'Cashier'
    Description VARCHAR(255) NULL
);
GO

-- 2. Users Table: Central for authentication and profiles 
CREATE TABLE Users (
    UserID INT PRIMARY KEY IDENTITY(1,1),
    Username VARCHAR(50) UNIQUE NOT NULL,
    Email VARCHAR(100) UNIQUE NOT NULL,
    NIC VARCHAR(20) UNIQUE NOT NULL,
    FirstName VARCHAR(50) NOT NULL,  
    LastName VARCHAR(50) NOT NULL,   
    Street VARCHAR(100) NULL,        
    City VARCHAR(50) NULL,           
    PostalCode VARCHAR(20) NULL,     
    DOB DATE NULL,
    PasswordHash VARCHAR(255) NOT NULL,  -- Store BCrypt hash
    RoleID INT NOT NULL FOREIGN KEY REFERENCES Roles(RoleID),  -- ON DELETE removed
    SecurityQuestion1 VARCHAR(255) NULL,
    SecurityAnswer1 VARCHAR(255) NULL,  -- Hash this in code
    SecurityQuestion2 VARCHAR(255) NULL,
    SecurityAnswer2 VARCHAR(255) NULL,  -- Hash this in code
    CreatedAt DATETIME DEFAULT GETDATE(),
    UpdatedAt DATETIME NULL,
    IsActive BIT DEFAULT 1
);
GO

-- 2a. UserPhones Table: New table for multivalued phone numbers (one-to-many with Users)
CREATE TABLE UserPhones (
    PhoneID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL FOREIGN KEY REFERENCES Users(UserID),  -- ON DELETE removed 
    PhoneNumber VARCHAR(20) NOT NULL,
    PhoneType VARCHAR(20) NULL  -- Optional: e.g., 'Mobile', 'Home', 'Work' for better organization
);
GO

-- 3. AccountTypes Table: Configurable account types with interest rates
CREATE TABLE AccountTypes (
    TypeID INT PRIMARY KEY IDENTITY(1,1),
    TypeName VARCHAR(20) UNIQUE NOT NULL,  -- e.g., 'Savings'
    InterestRate DECIMAL(5,2) NOT NULL,    -- e.g., 5.00
    Description VARCHAR(255) NULL
);
GO

-- 4. Accounts Table: Manages bank accounts with approval and accrual
CREATE TABLE Accounts (
    AccountID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL FOREIGN KEY REFERENCES Users(UserID),  -- ON DELETE removed
    TypeID INT NOT NULL FOREIGN KEY REFERENCES AccountTypes(TypeID),  -- ON DELETE removed
    AccountNumber VARCHAR(20) UNIQUE NOT NULL,
    Balance DECIMAL(18,2) DEFAULT 0.00,
    AccruedInterest DECIMAL(18,2) DEFAULT 0.00,
    LastAccrualDate DATE NULL,
    Status VARCHAR(20) NOT NULL,  -- e.g., 'Pending', 'Approved'
    ApprovedBy INT NULL FOREIGN KEY REFERENCES Users(UserID),  -- ON DELETE removed
    OpenedAt DATETIME NULL,
    ClosedAt DATETIME NULL,
    IsActive BIT DEFAULT 1
);
GO

-- 5. Loans Table: Handles loan applications and workflows
CREATE TABLE Loans (
    LoanID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL FOREIGN KEY REFERENCES Users(UserID),  -- ON DELETE removed
    LoanType VARCHAR(20) NOT NULL,  -- e.g., 'Personal'
    Amount DECIMAL(18,2) NOT NULL,
    InterestRate DECIMAL(5,2) NOT NULL,
    TermMonths INT NOT NULL,
    Status VARCHAR(20) NOT NULL,  -- e.g., 'Pending'
    ReviewedBy INT NULL FOREIGN KEY REFERENCES Users(UserID),  -- ON DELETE removed
    ApprovedBy INT NULL FOREIGN KEY REFERENCES Users(UserID),  -- ON DELETE removed
    AppliedAt DATETIME DEFAULT GETDATE(),
    Comments VARCHAR(500) NULL
);
GO

-- 6. LoanDocuments Table: Stores documents for loans
CREATE TABLE LoanDocuments (
    DocumentID INT PRIMARY KEY IDENTITY(1,1),
    LoanID INT NOT NULL FOREIGN KEY REFERENCES Loans(LoanID),  -- ON DELETE removed
    FilePath VARCHAR(255) NOT NULL,
    FileType VARCHAR(50) NOT NULL,
    UploadedAt DATETIME DEFAULT GETDATE()
);
GO

-- 7. Transactions Table: Logs financial transactions
CREATE TABLE Transactions (
    TransactionID INT PRIMARY KEY IDENTITY(1,1),
    AccountID INT NOT NULL FOREIGN KEY REFERENCES Accounts(AccountID),  -- ON DELETE removed
    TargetAccountID INT NULL FOREIGN KEY REFERENCES Accounts(AccountID),  -- ON DELETE removed
    Type VARCHAR(20) NOT NULL,  -- e.g., 'Deposit'
    Amount DECIMAL(18,2) NOT NULL,
    Description VARCHAR(255) NULL,
    ReferenceNumber VARCHAR(50) UNIQUE NOT NULL,
    PerformedBy INT NOT NULL FOREIGN KEY REFERENCES Users(UserID),  -- ON DELETE removed
    CreatedAt DATETIME DEFAULT GETDATE(),
    Status VARCHAR(20) DEFAULT 'Completed'
);
GO

-- 8. SupportTickets Table: Basic customer support ticketing
CREATE TABLE SupportTickets (
    TicketID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL FOREIGN KEY REFERENCES Users(UserID),  -- ON DELETE removed
    Subject VARCHAR(100) NOT NULL,
    Message TEXT NOT NULL,
    Status VARCHAR(20) NOT NULL,  -- e.g., 'Open'
    AssignedTo INT NULL FOREIGN KEY REFERENCES Users(UserID),  -- ON DELETE removed
    CreatedAt DATETIME DEFAULT GETDATE(),
    ResolvedAt DATETIME NULL
);
GO

-- 9. FAQs Table: Admin-managed FAQs
CREATE TABLE FAQs (
    FaqID INT PRIMARY KEY IDENTITY(1,1),
    Question VARCHAR(255) NOT NULL,
    Answer TEXT NOT NULL,
    Category VARCHAR(50) NULL,
    CreatedBy INT NOT NULL FOREIGN KEY REFERENCES Users(UserID),  -- ON DELETE removed
    CreatedAt DATETIME DEFAULT GETDATE(),
    UpdatedAt DATETIME NULL
);
GO

-- 10. BankNews Table: Public bank news and updates
CREATE TABLE BankNews (
    NewsID INT PRIMARY KEY IDENTITY(1,1),
    Title VARCHAR(100) NOT NULL,
    Content TEXT NOT NULL,
    Category VARCHAR(50) NOT NULL,
    PostedBy INT NOT NULL FOREIGN KEY REFERENCES Users(UserID),  -- ON DELETE removed
    PostedAt DATETIME DEFAULT GETDATE(),
    ExpiryDate DATE NULL,
    IsPublic BIT DEFAULT 1
);
GO

-- 11. Notifications Table: In-app notifications
CREATE TABLE Notifications (
    NotificationID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL FOREIGN KEY REFERENCES Users(UserID),  -- ON DELETE removed
    Message VARCHAR(255) NOT NULL,
    Type VARCHAR(20) NOT NULL,
    SentAt DATETIME DEFAULT GETDATE(),
    IsRead BIT DEFAULT 0
);
GO

-- 12. Feedback Table: User feedback storage
CREATE TABLE Feedback (
    FeedbackID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL FOREIGN KEY REFERENCES Users(UserID),  -- ON DELETE removed
    Message TEXT NOT NULL,
    SubmittedAt DATETIME DEFAULT GETDATE()
);
GO

-- 13. AuditLogs Table: Tracks key system actions for security
CREATE TABLE AuditLogs (
    LogID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT NULL FOREIGN KEY REFERENCES Users(UserID),  -- ON DELETE removed
    Action VARCHAR(50) NOT NULL,
    Details VARCHAR(500) NULL,
    Timestamp DATETIME DEFAULT GETDATE()
);
GO

-- Indexes for Performance 
CREATE INDEX IDX_Users_Name ON Users(LastName, FirstName);
CREATE INDEX IDX_Users_Email ON Users(Email);
CREATE INDEX IDX_Users_NIC ON Users(NIC);
CREATE INDEX IDX_Accounts_AccountNumber ON Accounts(AccountNumber);
CREATE INDEX IDX_Accounts_UserID ON Accounts(UserID);
CREATE INDEX IDX_Loans_UserID_Status ON Loans(UserID, Status);
CREATE INDEX IDX_Loans_UserID_LoanType ON Loans(UserID, LoanType);
CREATE INDEX IDX_Transactions_AccountID_CreatedAt ON Transactions(AccountID, CreatedAt DESC);
CREATE INDEX IDX_BankNews_PostedAt ON BankNews(PostedAt DESC);
CREATE INDEX IDX_UserPhones_UserID ON UserPhones(UserID);  
GO



