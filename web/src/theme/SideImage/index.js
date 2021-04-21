import React from 'react';
import styles from './index.module.css';

export default function SideImage({children, src, alt}) {
    return (<div id={styles.container}><img id={styles.float} src={src} alt={alt}/>{children}</div>);
}