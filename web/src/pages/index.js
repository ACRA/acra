import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './index.module.css';
import logo from '@site/static/img/logo.png'

export default function Home() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <Layout
            title={`${siteConfig.title}`}
            description="Application Crash Reports for Android">
            <main>
                <div className={clsx("container", styles.mainContent)}>
                    <p>Building quality Android apps and getting good reviews depends on your ability to know, understand and fix bugs when your users
                        experience them.</p>
                    <img src={logo} alt="ACRA Logo"/>
                    <p>ACRA catches exceptions, retrieves <a href="https://www.acra.ch/javadoc/latest/acra/org.acra/-report-field/">lots of context data</a> and sends them to
                        the <a href="https://www.acra.ch/docs/Backends">backend of your choice</a>.</p>
                    <p>Best of all, it is <a href="https://en.wikipedia.org/wiki/Apache_License">FREE</a> and <a href="https://github.com/ACRA/acra">OPEN
                        SOURCE</a>.</p>

                </div>
            </main>
        </Layout>
    );
}
