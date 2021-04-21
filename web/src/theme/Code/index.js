import React from 'react';
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

export default function Code({ children, type, languages }) {
    return (<Tabs
        defaultValue="kotlin"
        groupId={type}
        values={languages}>
        {children.map((child) => {
            var cprops = child.props.children.props;
            var lang = (cprops && cprops.className) ? cprops.className.replace(/^(language-)/, "") : "kotlin";
            return (<TabItem key={lang} value={lang}>{child}</TabItem>);
        })}
    </Tabs>);
}

export function GradleCode({ children }) {
    return (<Code type="gradle" languages={[
        { label: 'Kotlin', value: 'kotlin', },
        { label: 'Groovy', value: 'groovy', },
    ]}>{children}</Code>);
}

export function AndroidCode({ children }) {
    return (<Code type="android" languages={[
        { label: 'Kotlin', value: 'kotlin', },
        { label: 'Java', value: 'java', },
    ]}>{children}</Code>);
}