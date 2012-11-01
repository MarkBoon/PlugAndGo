CREATE TABLE go_pattern
(
	uniqueId INTEGER AUTO_INCREMENT PRIMARY KEY,
	groupId INTEGER,
	patternNr INTEGER,
	width INTEGER,
	height INTEGER,
	type INTEGER,
	startX INTEGER,
	startY INTEGER,
	blackX INTEGER,
	blackY INTEGER,
	whiteX INTEGER,
	whiteY INTEGER,
	userX INTEGER,
	userY INTEGER,
	topEdge BOOLEAN,
	leftEdge  BOOLEAN,
	bottomEdge  BOOLEAN,
	rightEdge BOOLEAN,
	text VARCHAR(1000),
	conditions VARCHAR(1000),
	blackBits0 BIGINT,
	blackBits1 BIGINT,
	blackBits2 BIGINT,
	blackBits3 BIGINT,

	whiteBits0 BIGINT,
	whiteBits1 BIGINT,
	whiteBits2 BIGINT,
	whiteBits3 BIGINT,

	emptyBits0 BIGINT,
	emptyBits1 BIGINT,
	emptyBits2 BIGINT,
	emptyBits3 BIGINT,

	urgencyValueBlack DOUBLE,
	urgencyValueWhite DOUBLE,
	blackNrOccurrences INTEGER,
	whiteNrOccurrences INTEGER,
	blackNrSuccesses INTEGER,
	whiteNrSuccesses INTEGER,

	generated BOOLEAN,
	createdDate TIMESTAMP
)