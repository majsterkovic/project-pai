import {Tag} from "./Tag";

export interface TextResource {
    id: string;
    name: string;
    content: string;
    tags: Tag[];
    imagePath: string;
}