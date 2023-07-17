package indi.dmzz_yyhyy.lightnovelreader.data.book

/**
{
    "data": {
        "bookID": 2152,
        "bookName": "熊熊勇闯异世界",
        "bookCoverURL": "http://img.wenku8.com/image/2/2152/2152s.jpg",
        "bookIntroduction": "　　请问您认为游戏比现实生活更好玩吗？─YES \r\n　　请问您在现实世界有重要的人吗？──NO \r\n　　……我，优奈在线上游戏中回答问卷，没想到却被送到了异世界（大概吧）。 \r\n　　我是家里蹲资历有三年的老练游戏玩家。 \r\n　　一开始穿在我身上的装备竟然是「熊熊套装」…… \r\n　　这什么鬼啊──！ \r\n　　可是又强又方便，那好吧。 \r\n　　打倒野狼、驱除哥布林，我就来当个最强的熊熊冒险者吧。"
    }
}
**/

data class Information(val data:Data) {
    data class Data(val bookID: Int, val bookName: String, val bookCoverURL: String, val bookIntroduction: String)
}