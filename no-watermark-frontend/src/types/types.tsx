export type Data<T> = { 
    code: number
    data: T
    msg: string
}


export type Platform = {
    name: string
    logo: string
}

export type MediaInfo = {
    title: string
    author: {
        nickname: string
        avatar: string
    }
    cover: string
    medias: {
        type: 'VIDEO'|'AUDIO'|'IMAGE'|'LIVE'
        url: string
        height: number
        width: number
    }[]
};